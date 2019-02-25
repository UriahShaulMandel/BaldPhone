/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.bald.uriah.baldphone.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

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
import com.bald.uriah.baldphone.utils.PageTransformers;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.utils.Toggeler;
import com.bald.uriah.baldphone.views.BaldImageButton;
import com.bald.uriah.baldphone.views.BatteryView;
import com.bald.uriah.baldphone.views.ViewPagerHolder;
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

public class HomeScreen extends BaldActivity {
    private static final String TAG = HomeScreen.class.getSimpleName();
    private static final int[] SOUND_DRAWABLES = new int[]{
            R.drawable.mute_on_background,
            R.drawable.vibration_on_background,
            R.drawable.sound_on_background
    };
    private static final IntentFilter BATTERY_FILTER = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private static int onStartCounter = 0;
    private static boolean flashState;
    public boolean finishedUpdatingApps, launchAppsActivity;
    private Point screenSize;
    private Lantern lantern;
    private SharedPreferences sharedPreferences;
    private BaldPrefsUtils baldPrefsUtils;
    private ViewPagerHolder viewPagerHolder;
    private BatteryView batteryView;
    private BaldImageButton notificationsButton, sosButton, soundButton, flashButton;
    private AudioManager audioManager;
    private BaldHomeWatcher baldHomeWatcher;
    private boolean flashInited;
    @NonNull
    public final NotesView.RecognizerManager recognizerManager = new NotesView.RecognizerManager();

    public BaldPagerAdapter baldPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        S.logImportant("HomeScreen was started!");
        sharedPreferences = BPrefs.get(this);

        if (!sharedPreferences.getBoolean(BPrefs.AFTER_TUTORIAL_KEY, false)) {
            startActivity(new Intent(this, TutorialActivity.class));
            finish();
            return;
        }

        //this is the way to inform user on updates that changes things.
//        if (!sharedPreferences.getBoolean("6.1.0", false)) {
//            sharedPreferences.edit().putBoolean("6.1.0", true).apply();
//            BDB
//                    .from(this)
//                    .setTitle(R.string.baldphone_updated)
//                    .setSubText(R.string.last_update_subtext)
//                    .setCancelable(true)
//                    .setDialogState(BDialog.DialogState.OK)
//                    .show();
//        }

        startService(new Intent(this, NotificationListenerService.class));
        new UpdateApps(this).execute(this.getApplicationContext());


        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);

        attachToXml();
        lantern = new Lantern(this)
                .observeLifecycle(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            flashInited = lantern.initTorch();
        }


        sosButton.setOnClickListener((v) -> {
            startActivity(new Intent(this, SOSActivity.class));
            overridePendingTransition(R.anim.slide_in_down, R.anim.nothing);
        });
        notificationsButton.setOnClickListener((v) -> {
            startActivity(new Intent(this, NotificationsActivity.class));
            overridePendingTransition(R.anim.slide_in_down, R.anim.nothing);
        });
        batteryView.setOnClickListener((v) -> BaldToast.from(this)
                .setText(batteryView.percentage + "%")
                .setBig(true)
                .setType(BaldToast.TYPE_INFORMATIVE)
                .show());
        baldPrefsUtils = BaldPrefsUtils.newInstance(this);
        viewPagerHandler();
        baldHomeWatcher = new BaldHomeWatcher(this, this::viewPagerStartHandler);
        recognizerManager.setHomeScreen(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        onStartCounter++;
        if (finishedUpdatingApps)
            viewPagerStartHandler();
        baldHomeWatcher.startWatch();
        final int percent = (int) (Math.random() * 100) + 1;//1 - 100
        if (percent < (100 + (onStartCounter - 20) * 5))
            if (percent % 3 == 1 && !isActivityDefault()) { // one in 3 chance
                onStartCounter = 0;
                BDB.from(this)
                        .setTitle(R.string.set_home_screen)
                        .setSubText(R.string.set_home_screen_subtext)
                        .setDialogState(BDialog.DialogState.OK_NO)
                        .setPositiveButtonListener(params -> {
                            FakeLauncherActivity.resetPreferredLauncherAndOpenChooser(this);
                            return true;
                        })
                        .show();
            } else if (percent > 99 && Math.random() < 0.2) {
                onStartCounter = 0;
                S.shareBaldPhone(this);
            }
    }


    /* the security exception will happen only after api 23 so please stfu*/
    @SuppressLint("InlinedApi")
    protected void onResume() {
        super.onResume();
        if (baldPrefsUtils.hasChanged(this)) {
            viewPagerHolder.getViewPager().removeAllViews();//android auto saves fragments, not good for us in this case
            this.recreate();
        }
        Toggeler.newAdvancedImageToggeler(
                soundButton,
                soundButton,
                SOUND_DRAWABLES,
                index -> {
                    try {
                        audioManager.setRingerMode(index);
                    } catch (SecurityException e) {
                        startActivity(new Intent(
                                android.provider.Settings
                                        .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                    }
                },
                audioManager.getRingerMode(),
                -1
        );

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
                    .putExtra(PermissionActivity.EXTRA_NAME, activityName())));

        } else {
            flashButton.setOnClickListener(D.EMPTY_CLICK_LISTENER);
            flashButton.setVisibility(View.GONE);
        }

        LocalBroadcastManager.getInstance(this).

                registerReceiver(notificationReceiver,
                        new IntentFilter(NotificationListenerService.HOME_SCREEN_ACTIVITY_BROADCAST));
        LocalBroadcastManager.getInstance(this).

                sendBroadcast(
                        new Intent(ACTION_REGISTER_ACTIVITY)
                                .

                                        putExtra(KEY_EXTRA_ACTIVITY, NOTIFICATIONS_HOME_SCREEN));

        registerReceiver(batteryReceiver, BATTERY_FILTER);
    }


    @Override
    protected void onPause() {
        //this is *NOT* bad practice.
        //read https://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error
        //android platform may unregister the receiver without asking anyone, and this is the best solution.
        //that's a known bug.
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

    //<Receivers>
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (batteryView != null) {
                final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                final int batteryPct = Math.round(level / (float) scale * 100);
                batteryView.setLevel(batteryPct, intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1));
            }
        }
    };

    private Handler handler = new Handler();
    private final Runnable shakeIt = new Runnable() {
        @Override
        public void run() {
            final Drawable d = notificationsButton.getDrawable();
            if (d instanceof AnimatedVectorDrawable) {
                final AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) d;
                animatedVectorDrawable.start();

                handler.postDelayed(this, 10 * D.SECOND);
            }
        }
    };


    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int amount = intent.getIntExtra("amount", -1);
            final @DrawableRes int drawable;
            switch (amount) {
                case NOTIFICATIONS_NONE:
                    drawable = R.drawable.notification_none_on_background;
                    break;
                case NOTIFICATIONS_SOME:
                    drawable = R.drawable.notification_some_on_background;
                    break;
                case NOTIFICATIONS_ALOT:
                    drawable = R.drawable.notification_alot_on_background;
                    break;
                default:
                    throw new AssertionError();
            }
            notificationsButton.setImageResource(drawable);

            handler.removeCallbacks(shakeIt);
            handler.postDelayed(shakeIt, 5 * D.SECOND);
        }
    };
    //</Receivers>


    private void viewPagerHandler() {
        baldPagerAdapter = new BaldPagerAdapter(this);
        viewPagerHolder.setPageTransformer(false, PageTransformers.pageTransformers[sharedPreferences.getInt(BPrefs.PAGE_TRANSFORMERS_KEY, BPrefs.PAGE_TRANSFORMERS_DEFAULT_VALUE)]);
        viewPagerHolder.setViewPagerAdapter(baldPagerAdapter);
        viewPagerHolder.setCurrentItem(baldPagerAdapter.startingPage);
    }

    private void viewPagerStartHandler() {
        baldPagerAdapter.obtainAppList();
        viewPagerHolder.setCurrentItem(baldPagerAdapter.startingPage);
        viewPagerHolder.notifyDataChanegd();
    }

    private void attachToXml() {
        setContentView(R.layout.home_screen);

        final ViewGroup top_bar = findViewById(R.id.top_bar);
        int tmpPadding = Math.min(screenSize.x, screenSize.y) / 45;
        for (int i = 0; i < top_bar.getChildCount(); i++) {
            top_bar.getChildAt(i).setPadding(tmpPadding, tmpPadding, tmpPadding, tmpPadding);
        }

        viewPagerHolder = findViewById(R.id.view_pager_holder);
        sosButton = findViewById(R.id.sos);
        soundButton = findViewById(R.id.sound);
        batteryView = findViewById(R.id.battery);
        notificationsButton = findViewById(R.id.notifications);
        flashButton = findViewById(R.id.flash);
    }


    @Override
    public void onBackPressed() {
        if (vibrator != null)
            vibrator.vibrate(D.vibetime);
        viewPagerStartHandler();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

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


    static class UpdateApps extends AsyncTask<Context, Void, Void> {
        final WeakReference<HomeScreen> homeScreenWeakReference;

        public UpdateApps(HomeScreen homeScreen) {
            super();
            homeScreenWeakReference = new WeakReference<>(homeScreen);
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            AppsDatabaseHelper.updateDB(contexts[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HomeScreen homeScreen = homeScreenWeakReference.get();
            if (homeScreen != null) {
                homeScreen.viewPagerStartHandler();
                homeScreen.finishedUpdatingApps = true;
                if (homeScreen.launchAppsActivity)
                    homeScreen.startActivity(new Intent(homeScreen, AppsActivity.class));
            }
        }

    }


    private static final int SPEECH_REQUEST_CODE = 7;

    public void displaySpeechRecognizer() {
        startActivityForResult(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        ),
                SPEECH_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            final String spokenText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            recognizerManager.onSpeechRecognizerResult(spokenText);
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
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
}