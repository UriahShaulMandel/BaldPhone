/*
 * Copyright 2019 Uriah Shaul Mandel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bald.uriah.baldphone.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.BaldPagerAdapter;
import com.bald.uriah.baldphone.databases.apps.AppsDatabaseHelper;
import com.bald.uriah.baldphone.services.NotificationListenerService;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldHomeWatcher;
import com.bald.uriah.baldphone.utils.BaldPrefsUtils;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.DropDownRecyclerViewAdapter;
import com.bald.uriah.baldphone.utils.PageTransformers;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.utils.UpdatingUtil;
import com.bald.uriah.baldphone.views.BaldImageButton;
import com.bald.uriah.baldphone.views.BatteryView;
import com.bald.uriah.baldphone.views.ViewPagerHolder;
import com.bald.uriah.baldphone.views.home.HomePage1;
import com.bald.uriah.baldphone.views.home.NotesView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import github.nisrulz.lantern.Lantern;

import static com.bald.uriah.baldphone.services.NotificationListenerService.ACTION_REGISTER_ACTIVITY;
import static com.bald.uriah.baldphone.services.NotificationListenerService.ACTIVITY_NONE;
import static com.bald.uriah.baldphone.services.NotificationListenerService.KEY_EXTRA_ACTIVITY;
import static com.bald.uriah.baldphone.services.NotificationListenerService.NOTIFICATIONS_ALOT;
import static com.bald.uriah.baldphone.services.NotificationListenerService.NOTIFICATIONS_HOME_SCREEN;
import static com.bald.uriah.baldphone.services.NotificationListenerService.NOTIFICATIONS_NONE;
import static com.bald.uriah.baldphone.services.NotificationListenerService.NOTIFICATIONS_SOME;

public class HomeScreenActivity extends BaldActivity {
    private static final String TAG = HomeScreenActivity.class.getSimpleName();

    private static final int[]
            SOUND_DRAWABLES = {R.drawable.mute_on_background, R.drawable.vibration_on_background, R.drawable.sound_on_background},
            SOUND_TEXTS = {R.string.mute, R.string.vibrate, R.string.sound};

    private static final IntentFilter BATTERY_FILTER = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private static final int SPEECH_REQUEST_CODE = 7;

    private static int onStartCounter = 0;
    private static boolean flashState;

    @NonNull
    public final NotesView.RecognizerManager recognizerManager = new NotesView.RecognizerManager();

    public boolean finishedUpdatingApps, launchAppsActivity;
    public BaldPagerAdapter baldPagerAdapter;

    private Point screenSize;
    private Lantern lantern;
    private SharedPreferences sharedPreferences;
    private BaldPrefsUtils baldPrefsUtils;
    private ViewPagerHolder viewPagerHolder;
    private BatteryView batteryView;
    private boolean lowBatteryAlert;
    /**
     * Listens to changes in battery {@value Intent#ACTION_BATTERY_CHANGED}
     */
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (batteryView != null) {
                final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                final int batteryPct = Math.round(level / (float) scale * 100);
                final int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                final boolean charged = chargePlug == BatteryManager.BATTERY_PLUGGED_AC || chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS || chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                batteryView.setLevel(batteryPct, charged);
                if (lowBatteryAlert)
                    getWindow().setStatusBarColor((batteryPct < D.LOW_BATTERY_LEVEL && !charged) ? ContextCompat.getColor(context, R.color.battery_low) : D.DEFAULT_STATUS_BAR_COLOR);
            }
        }
    };
    private int notificationCount = 0;
    @ColorInt
    private int decorationColorOnBackground;
    private BaldImageButton notificationsButton, sosButton, soundButton, flashButton;
    private AudioManager audioManager;
    private BaldHomeWatcher baldHomeWatcher;
    private boolean flashInited;
    private Handler handler = new Handler();
    /**
     * "Shakes" the notifications icon when it has more than {@value NotificationListenerService#NOTIFICATIONS_ALOT}
     */
    private final Runnable shakeIt = new Runnable() {
        @Override
        public void run() {
            final Drawable d = notificationsButton.getDrawable();
            if (d instanceof AnimatedVectorDrawable) {
                final AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) d;
                animatedVectorDrawable.start();
                final int minusSeconds = Math.min((int) (Math.max((notificationCount - NOTIFICATIONS_ALOT) * 0.5f, 0)), 7);
                handler.postDelayed(this, (10 - minusSeconds) * D.SECOND);
            }
        }
    };
    /**
     * Listens to broadcasts from {@link NotificationListenerService}
     * This listener only gets the number of notifications and updates {@link HomeScreenActivity#notificationsButton}
     * The red dot is being updated via {@link HomePage1#notificationReceiver}
     */
    public final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notificationCount = intent.getIntExtra("amount", -1);
            if (notificationCount >= NOTIFICATIONS_ALOT) {
                final Drawable drawable = getDrawable(R.drawable.notification_alot_on_background);
                final float opacity = Math.min(((notificationCount - NOTIFICATIONS_ALOT) / 10.0f), 1.0f);
                drawable.setTint(S.blendColors(decorationColorOnBackground, getResources().getColor(R.color.battery_low), 1 - opacity));
                notificationsButton.setImageDrawable(drawable);
            } else if (notificationCount >= NOTIFICATIONS_SOME) {
                notificationsButton.setImageResource(R.drawable.notification_some_on_background);
            } else if (notificationCount >= NOTIFICATIONS_NONE) {
                notificationsButton.setImageResource(R.drawable.notification_none_on_background);
            } else {
                notificationsButton.setImageResource(R.drawable.error_on_background);
            }

            handler.removeCallbacks(shakeIt);
            handler.postDelayed(shakeIt, 5 * D.SECOND);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        S.logImportant("HomeScreenActivity was started!");
        sharedPreferences = BPrefs.get(this);

        if (!sharedPreferences.getBoolean(BPrefs.AFTER_TUTORIAL_KEY, false) && !testing) {
            startActivity(new Intent(this, TutorialActivity.class));
            finish();
            return;
        }
        try {
            startService(new Intent(this, NotificationListenerService.class));
        } catch (Exception e) {
            Log.e(TAG, S.str(e.getMessage()));
            e.printStackTrace();
            BaldToast.from(this).setType(BaldToast.TYPE_ERROR).setText("Could not start Notification Listener Service!").show();
        }
        new UpdateApps(this).execute(this.getApplicationContext());
        lowBatteryAlert = sharedPreferences.getBoolean(BPrefs.LOW_BATTERY_ALERT_KEY, BPrefs.LOW_BATTERY_ALERT_DEFAULT_VALUE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);

        final TypedValue typedValue = new TypedValue();
        final Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.bald_decoration_on_background, typedValue, true);
        decorationColorOnBackground = typedValue.data;

        if ((sharedPreferences.getInt(BPrefs.STATUS_BAR_KEY, BPrefs.STATUS_BAR_DEFAULT_VALUE) == 1)) {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        attachToXml();
        lantern = new Lantern(this)
                .observeLifecycle(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            flashInited = lantern.initTorch();
        }

        if (sharedPreferences.getBoolean(BPrefs.EMERGENCY_BUTTON_VISIBLE_KEY, BPrefs.EMERGENCY_BUTTON_VISIBLE_DEFAULT_VALUE))
            sosButton.setOnClickListener((v) -> {
                startActivity(new Intent(this, SOSActivity.class));
                overridePendingTransition(R.anim.slide_in_down, R.anim.nothing);
            });
        else
            sosButton.setVisibility(View.GONE);
        notificationsButton.setOnClickListener((v) -> {
            startActivity(new Intent(this, NotificationsActivity.class));
            overridePendingTransition(R.anim.slide_in_down, R.anim.nothing);
        });
        soundButton.setOnClickListener(v -> S.showDropDownPopup(this, getWindow().getDecorView().getWidth(), new DropDownRecyclerViewAdapter.DropDownListener() {
            @SuppressLint("InlinedApi")
            @Override
            public void onUpdate(DropDownRecyclerViewAdapter.ViewHolder viewHolder, final int position, PopupWindow popupWindow) {
                viewHolder.pic.setImageResource(SOUND_DRAWABLES[position]);
                viewHolder.text.setText(SOUND_TEXTS[position]);
                viewHolder.itemView.setOnClickListener(v1 -> {
                    try {
                        audioManager.setRingerMode(position);
                        soundButton.setImageResource(SOUND_DRAWABLES[position]);
                    } catch (SecurityException e) {
                        startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                    }
                    popupWindow.dismiss();
                });
            }

            @Override
            public int size() {
                return 3;
            }
        }, soundButton));
        batteryView.setOnClickListener((v) -> BaldToast.from(this)
                .setText(batteryView.percentage + "%")
                .setBig(true)
                .setType(BaldToast.TYPE_INFORMATIVE)
                .show());
        baldPrefsUtils = BaldPrefsUtils.newInstance(this);
        viewPagerHandler();
        baldHomeWatcher = new BaldHomeWatcher(this, this::updateViewPager);
        recognizerManager.setHomeScreen(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        onStartCounter++;
        if (finishedUpdatingApps)
            updateViewPager();
        baldHomeWatcher.startWatch();
        if (!testing) { // TODO replace with system env
            final int percent = (int) (Math.random() * 100) + 1;//1 - 100
            if (percent < (100 + (onStartCounter - 20) * 5))
                if (percent % 3 == 1 && !isActivityDefault()) { // one in 3 chance
                    onStartCounter = 0;
                    BDB.from(this)
                            .setTitle(R.string.set_home_screen)
                            .setSubText(R.string.set_home_screen_subtext)
                            .addFlag(BDialog.FLAG_OK | BDialog.FLAG_NO)
                            .setPositiveButtonListener(params -> {
                                FakeLauncherActivity.resetPreferredLauncherAndOpenChooser(this);
                                return true;
                            })
                            .show();
                } else if (percent > 99 && Math.random() < 0.1) {
                    onStartCounter = 0;
                    S.shareBaldPhone(this);
                } else if (percent > 95) {
                    //noinspection ConstantConditions
                    if (BuildConfig.FLAVOR.equals("baldUpdates"))
                        if (sharedPreferences.getLong(BPrefs.LAST_UPDATE_ASKED_VERSION_KEY, 0) + 2 * D.DAY < System.currentTimeMillis()) {
                            UpdatingUtil.checkForUpdates(this, false);
                        }
                }
        }

    }

    /* the security exception will happen only after api 23 so Lint please shush*/
    @SuppressLint("InlinedApi")
    protected void onResume() { // remember to change in Page1EditorActivity.java too!
        super.onResume();
        if (baldPrefsUtils.hasChanged(this)) {
            viewPagerHolder.getViewPager().removeAllViews();//android auto saves fragments, not good for us in this case
            this.recreate();
        }

        soundButton.setImageResource(SOUND_DRAWABLES[audioManager.getRingerMode()]);
        flashButton.setImageResource(flashState ?
                R.drawable.flashlight_on_background :
                R.drawable.flashlight_off_on_background);
        if (flashInited) {
            flashButton.setOnClickListener((v) -> {
                flashState = !flashState;
                lantern.enableTorchMode(true);
                if (!flashState) // looks weird (it is) but necessary. otherwise it wont turn off after device rotation...
                    lantern.enableTorchMode(false);
                flashButton.setImageResource(flashState ?
                        R.drawable.flashlight_on_background :
                        R.drawable.flashlight_off_on_background);
            });
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            flashButton.setOnClickListener(v -> startActivity(new Intent(this, PermissionActivity.class)
                    .putExtra(PermissionActivity.EXTRA_REQUIRED_PERMISSIONS, PERMISSION_CAMERA)
            ));

        } else if (!testing) { // For travis screenshots to show the flashlight
            flashButton.setOnClickListener(D.EMPTY_CLICK_LISTENER);
            flashButton.setVisibility(View.GONE);
        }

        LocalBroadcastManager.getInstance(this).
                registerReceiver(notificationReceiver,
                        new IntentFilter(NotificationListenerService.HOME_SCREEN_ACTIVITY_BROADCAST));
        handler.postDelayed(() -> LocalBroadcastManager.getInstance(this).
                sendBroadcast(
                        new Intent(ACTION_REGISTER_ACTIVITY)
                                .putExtra(KEY_EXTRA_ACTIVITY, NOTIFICATIONS_HOME_SCREEN)), 200 * D.MILLISECOND);

        registerReceiver(batteryReceiver, BATTERY_FILTER);
    }

    @Override
    protected void onPause() {
        //read https://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error
        //android platform may unregister the receiver without asking anyone, and this is the best solution.
        //first occurred in LG k10 api level 23
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
        } catch (IllegalArgumentException ignore) {
        }
        try {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_REGISTER_ACTIVITY).putExtra(KEY_EXTRA_ACTIVITY, ACTIVITY_NONE));
        } catch (IllegalArgumentException ignore) {
        }
        try {
            unregisterReceiver(batteryReceiver);
        } catch (IllegalArgumentException ignore) {
        }
        handler.removeCallbacks(shakeIt);
        super.onPause();
    }

    @Override
    protected void onStop() {
        baldHomeWatcher.stopWatch();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        recognizerManager.setHomeScreen(null);
        super.onDestroy();
    }

    /**
     * Starts the view pager - being called only in {@link #onCreate(Bundle)}
     */
    private void viewPagerHandler() {
        baldPagerAdapter = new BaldPagerAdapter(this);
        viewPagerHolder.setPageTransformer(false, PageTransformers.pageTransformers[sharedPreferences.getInt(BPrefs.PAGE_TRANSFORMERS_KEY, BPrefs.PAGE_TRANSFORMERS_DEFAULT_VALUE)]);
        viewPagerHolder.setViewPagerAdapter(baldPagerAdapter);
        viewPagerHolder.setCurrentItem(baldPagerAdapter.startingPage);
    }

    /**
     * Updates {@link HomeScreenActivity#baldPagerAdapter} apps
     * Sets the page to {@link BaldPagerAdapter#startingPage}
     */
    private void updateViewPager() {
        baldPagerAdapter.obtainAppList();
        viewPagerHolder.setCurrentItem(baldPagerAdapter.startingPage);
        viewPagerHolder.notifyDataChanegd();
    }

    private void attachToXml() {
        setContentView(R.layout.home_screen);
        viewPagerHolder = findViewById(R.id.view_pager_holder);

        final ViewGroup top_bar = findViewById(R.id.top_bar);
        int tmpPadding = Math.min(screenSize.x, screenSize.y) / 45;
        for (int i = 0; i < top_bar.getChildCount(); i++) {
            top_bar.getChildAt(i).setPadding(tmpPadding, tmpPadding, tmpPadding, tmpPadding);
        }

        sosButton = top_bar.findViewById(R.id.sos);
        soundButton = top_bar.findViewById(R.id.sound);
        batteryView = top_bar.findViewById(R.id.battery);
        notificationsButton = top_bar.findViewById(R.id.notifications);
        flashButton = top_bar.findViewById(R.id.flash);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        try {
            super.startActivity(intent, options);
        } catch (Exception e) {
            Log.e(TAG, S.str(e.getMessage()));
            e.printStackTrace();
            BaldToast.error(this);
        }
    }

    private boolean isActivityDefault() {
        final List<IntentFilter> filters = new ArrayList<>();
        final ComponentName myComponentName = getComponentName();
        final String myPackageName = myComponentName.getPackageName();
        final List<ComponentName> activities = new ArrayList<>();
        final PackageManager packageManager = getPackageManager();
        packageManager.getPreferredActivities(filters, activities, myPackageName);
        return (activities.contains(myComponentName));
    }

    public void displaySpeechRecognizer() {
        try {
            startActivityForResult(
                    new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            .putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            ),
                    SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, S.str(e.getMessage()));
            e.printStackTrace();
            BaldToast.error(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            final String spokenText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            recognizerManager.onSpeechRecognizerResult(spokenText);
        }
    }

    @Override
    public void onBackPressed() {
        if (vibrator != null)
            vibrator.vibrate(D.vibetime);
        updateViewPager();
    }

    static class UpdateApps extends AsyncTask<Context, Void, Void> {
        final WeakReference<HomeScreenActivity> homeScreenWeakReference;

        UpdateApps(HomeScreenActivity homeScreen) {
            super();
            homeScreenWeakReference = new WeakReference<>(homeScreen);
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            try {
                AppsDatabaseHelper.updateDB(contexts[0]);
            } catch (Exception e) {
                BaldToast.from(contexts[0].getApplicationContext()).setType(BaldToast.TYPE_ERROR).setLength(Toast.LENGTH_LONG).setText(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HomeScreenActivity homeScreen = homeScreenWeakReference.get();
            if (homeScreen != null) {
                homeScreen.updateViewPager();
                homeScreen.finishedUpdatingApps = true;
                if (homeScreen.launchAppsActivity)
                    homeScreen.startActivity(new Intent(homeScreen, AppsActivity.class));
            }
        }

    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}