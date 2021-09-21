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

package com.bald.uriah.baldphone.views.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.provider.Telephony;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.apps.applications.AppsActivity;
import com.bald.uriah.baldphone.apps.dialer.DialerActivity;
import com.bald.uriah.baldphone.activities.HomeScreenActivity;
import com.bald.uriah.baldphone.activities.Page1EditorActivity;
import com.bald.uriah.baldphone.apps.recent_calls.RecentActivity;
import com.bald.uriah.baldphone.apps.alarms.AlarmsActivity;
import com.bald.uriah.baldphone.apps.contacts.ContactsActivity;
import com.bald.uriah.baldphone.apps.media.PhotosActivity;
import com.bald.uriah.baldphone.apps.media.VideosActivity;
import com.bald.uriah.baldphone.apps.pills.PillsActivity;
import com.bald.uriah.baldphone.apps.applications.App;
import com.bald.uriah.baldphone.apps.applications.AppsDatabase;
import com.bald.uriah.baldphone.apps.applications.AppsDatabaseHelper;
import com.bald.uriah.baldphone.apps.recent_calls.CallLogsHelper;
import com.bald.uriah.baldphone.services.NotificationListenerService;
import com.bald.uriah.baldphone.core.BDB;
import com.bald.uriah.baldphone.core.BDialog;
import com.bald.uriah.baldphone.core.BPrefs;
import com.bald.uriah.baldphone.core.BaldToast;
import com.bald.uriah.baldphone.utils.PackageUtils;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.FirstPageAppIcon;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.bald.uriah.baldphone.apps.applications.AppsDatabaseHelper.baldComponentNameBeginning;
import static com.bald.uriah.baldphone.services.NotificationListenerService.ACTION_REGISTER_ACTIVITY;
import static com.bald.uriah.baldphone.services.NotificationListenerService.KEY_EXTRA_ACTIVITY;
import static com.bald.uriah.baldphone.services.NotificationListenerService.NOTIFICATIONS_HOME_SCREEN;

@SuppressLint("ViewConstructor")
public class HomePage1 extends HomeView {
    public static final String TAG = HomePage1.class.getSimpleName();
    private final static String WHATSAPP_PACKAGE_NAME = "com.whatsapp";
    private final static ComponentName WHATSAPP_COMPONENT_NAME =
            new ComponentName(WHATSAPP_PACKAGE_NAME, WHATSAPP_PACKAGE_NAME + ".Main");
    public Map<App, FirstPageAppIcon> viewsToApps;
    /**
     * Listens to broadcasts from {@link NotificationListenerService}
     * This listener only checks if there are new messages\whatsapps,
     * and updates {@link HomePage1#bt_messages} and {@link HomePage1#bt_whatsapp} according to it
     * The notification icon is being updated via {@link HomeScreenActivity#notificationReceiver}
     */
    public final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Set<String> packagesSet = new HashSet<>(intent.getStringArrayListExtra("packages"));
            if (!viewsToApps.containsValue(bt_whatsapp))
                bt_whatsapp.setBadgeVisibility(packagesSet.contains(PackageUtils.WHATSAPP_PACKAGE_NAME));
            if (!viewsToApps.containsValue(bt_recent))
                bt_recent.setBadgeVisibility(!CallLogsHelper.isAllReadSafe(getContext().getContentResolver()));
            if (!viewsToApps.containsValue(bt_messages))
                bt_messages.setBadgeVisibility(packagesSet.contains(Telephony.Sms.getDefaultSmsPackage(context)));
            for (final App app : viewsToApps.keySet()) {
                viewsToApps.get(app).setBadgeVisibility(packagesSet.contains(ComponentName.unflattenFromString(app.getFlattenComponentName()).getPackageName()));
            }

        }
    };
    private View view;
    private FirstPageAppIcon bt_clock, bt_camera, bt_videos, bt_assistant, bt_messages, bt_photos, bt_contacts, bt_dialer, bt_whatsapp, bt_apps, bt_reminders, bt_recent;
    private boolean registered = false;
    private SharedPreferences sharedPreferences;

    public HomePage1(@NonNull Context context) {
        super((context instanceof HomeScreenActivity) ? (HomeScreenActivity) context : null, (Activity) context);
        sharedPreferences = BPrefs.get(activity);
    }

    public HomePage1(@NonNull Context context, AttributeSet attributeSet) {
        this(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container) {
        viewsToApps = new ArrayMap<>();
        view = inflater.inflate(R.layout.fragment_home_page1, container, false);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            view.findViewById(R.id.clock).setVisibility(View.GONE);

        attachXml();
        genOnClickListeners();
        return view;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!registered) {
            LocalBroadcastManager.getInstance(activity)
                    .registerReceiver(notificationReceiver,
                            new IntentFilter(NotificationListenerService.HOME_SCREEN_ACTIVITY_BROADCAST));
            registered = true;
            LocalBroadcastManager.getInstance(activity).sendBroadcast(
                    new Intent(ACTION_REGISTER_ACTIVITY)
                            .putExtra(KEY_EXTRA_ACTIVITY, NOTIFICATIONS_HOME_SCREEN));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (registered) {
            LocalBroadcastManager.getInstance(activity)
                    .unregisterReceiver(notificationReceiver);
            registered = false;
        }
    }

    private void attachXml() {
        bt_apps = view.findViewById(R.id.bt_apps);
        bt_contacts = view.findViewById(R.id.bt_contacts);
        bt_dialer = view.findViewById(R.id.bt_dialer);
        bt_whatsapp = view.findViewById(R.id.bt_whatsapp);
        bt_clock = view.findViewById(R.id.bt_clock);
        bt_reminders = view.findViewById(R.id.bt_reminders);
        bt_recent = view.findViewById(R.id.bt_recent);
        bt_camera = view.findViewById(R.id.bt_camera);
        bt_videos = view.findViewById(R.id.bt_videos);
        bt_photos = view.findViewById(R.id.bt_photos);
        bt_messages = view.findViewById(R.id.bt_messages);
        bt_assistant = view.findViewById(R.id.bt_assistant);
    }

    private Intent getCameraIntent() {
        final Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        final ActivityInfo activityInfo = activity.getPackageManager().resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo;
        final ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .setComponent(name);

    }

    private void genOnClickListeners() {
        sharedPreferences = BPrefs.get(activity);
        setupButton(BPrefs.CUSTOM_RECENTS_KEY, bt_recent, v -> homeScreen.startActivity(new Intent(homeScreen, RecentActivity.class)));
        setupButton(BPrefs.CUSTOM_DIALER_KEY, bt_dialer, v -> homeScreen.startActivity(new Intent(homeScreen, DialerActivity.class)));
        setupButton(BPrefs.CUSTOM_CONTACTS_KEY, bt_contacts, v -> homeScreen.startActivity(new Intent(homeScreen, ContactsActivity.class)));
        setupButton(BPrefs.CUSTOM_APP_KEY, bt_whatsapp, v -> {
            if (S.isPackageInstalled(homeScreen, WHATSAPP_PACKAGE_NAME))
                S.startComponentName(homeScreen, WHATSAPP_COMPONENT_NAME);
            else
                try {
                    homeScreen.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + WHATSAPP_PACKAGE_NAME)));
                } catch (android.content.ActivityNotFoundException e) {
                    homeScreen.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + WHATSAPP_PACKAGE_NAME)));
                }
        });
        setupButton(BPrefs.CUSTOM_ASSISTANT_KEY, bt_assistant, v -> {
            try {
                homeScreen.startActivity(new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                BaldToast.from(homeScreen).setType(BaldToast.TYPE_ERROR).setText(R.string.your_phone_doesnt_have_assistant_installed).show();
            }
        });
        setupButton(BPrefs.CUSTOM_MESSAGES_KEY, bt_messages, v -> {
            try {
                final ResolveInfo resolveInfo =
                        homeScreen.getPackageManager()
                                .queryIntentActivities(
                                        new Intent("android.intent.action.MAIN", null)
                                                .setPackage(Telephony.Sms.getDefaultSmsPackage(homeScreen))
                                        , 0)
                                .iterator()
                                .next();
                S.startComponentName(homeScreen, new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));

            } catch (Exception e) {
                BaldToast.from(homeScreen).setType(BaldToast.TYPE_ERROR).setText(R.string.an_error_has_occurred).show();
            }
        });
        setupButton(BPrefs.CUSTOM_PHOTOS_KEY, bt_photos, v -> homeScreen.startActivity(new Intent(homeScreen, PhotosActivity.class)));
        setupButton(BPrefs.CUSTOM_CAMERA_KEY, bt_camera, v -> homeScreen.startActivity(getCameraIntent()));
        setupButton(BPrefs.CUSTOM_VIDEOS_KEY, bt_videos, v -> homeScreen.startActivity(new Intent(homeScreen, VideosActivity.class)));
        setupButton(BPrefs.CUSTOM_PILLS_KEY, bt_reminders, v -> homeScreen.startActivity(new Intent(homeScreen, PillsActivity.class)));
        setupButton(BPrefs.CUSTOM_APPS_KEY, bt_apps, v -> {
            if (!homeScreen.finishedUpdatingApps)
                homeScreen.launchAppsActivity = true;
            else
                homeScreen.startActivity(new Intent(HomePage1.this.homeScreen, AppsActivity.class));
        });
        setupButton(BPrefs.CUSTOM_ALARMS_KEY, bt_clock, v -> homeScreen.startActivity(new Intent(homeScreen, AlarmsActivity.class)));
    }

    private void setupButton(String bPrefsKey, FirstPageAppIcon bt, View.OnClickListener onClickListener) {
        final App app;
        boolean phone = false;
        if (bt == bt_whatsapp && BuildConfig.FLAVOR.equals("gPlay") && !sharedPreferences.contains(bPrefsKey)) {
            app = AppsDatabase.getInstance(homeScreen).appsDatabaseDao().findByFlattenComponentName(baldComponentNameBeginning + Page1EditorActivity.class.getName());
        } else if (bt == bt_recent && BuildConfig.FLAVOR.equals("gPlay") && !sharedPreferences.contains(bPrefsKey)) {
            app = null;
            phone = true;
        } else if (sharedPreferences.contains(bPrefsKey)) {
            app = AppsDatabase.getInstance(homeScreen).appsDatabaseDao().findByFlattenComponentName(sharedPreferences.getString(bPrefsKey, null));
            if (app == null)
                sharedPreferences.edit().remove(bPrefsKey).apply();
        } else app = null;
        if (homeScreen != null) {
            if (app == null) {
                if (phone) {
                    bt.setOnClickListener(v -> activity.startActivity(S.getPhoneIntent(activity)));
                    bt.setText(R.string.phone);
                    bt.setImageResource(R.drawable.phone_on_button);
                } else {
                    bt.setOnClickListener(onClickListener);
                }
            } else {
                bt.setText(app.getLabel());
                AppsDatabaseHelper.loadPic(app, bt.imageView);
                bt.setOnClickListener(v -> S.startComponentName(homeScreen, ComponentName.unflattenFromString(app.getFlattenComponentName())));
                viewsToApps.put(app, bt);
            }
        } else {
            final Page1EditorActivity page1EditorActivity = (Page1EditorActivity) activity;
            final CharSequence initialAppName;
            if (BuildConfig.FLAVOR.equals("gPlay")) {
                if (bt == bt_whatsapp) {
                    initialAppName = activity.getString(R.string.edit_home_screen);
                } else if (bt == bt_recent)
                    initialAppName = activity.getString(R.string.phone);
                else initialAppName = bt.getText();
            } else {
                initialAppName = bt.getText();
            }
            final BDB bdb = BDB.from(activity)
                    .setTitle(R.string.custom_app)
                    .setSubText(R.string.custom_app_subtext)
                    .addFlag(BDialog.FLAG_OK | BDialog.FLAG_CANCEL)
                    .setOptions(initialAppName, activity.getText(R.string.custom))
                    .setOptionsStartingIndex(() -> sharedPreferences.contains(bPrefsKey) ? 1 : 0)
                    .setPositiveButtonListener(params -> {
                        if (params[0].equals(0)) {
                            sharedPreferences.edit().remove(bPrefsKey).apply();
                        } else
                            activity.startActivityForResult(new Intent(activity, AppsActivity.class).putExtra(AppsActivity.CHOOSE_MODE, bPrefsKey), AppsActivity.REQUEST_SELECT_CUSTOM_APP);
                        return true;
                    });

            bt.setOnClickListener(v -> bdb.show().setOnDismissListener(dialog -> {
                if (page1EditorActivity.baldPrefsUtils.hasChanged(page1EditorActivity)) {
                    page1EditorActivity.recreate();
                }
            }));

            if (app != null) {
                bt.setText(app.getLabel());
                AppsDatabaseHelper.loadPic(app, bt.imageView);
                viewsToApps.put(app, bt);
            }
            if (phone) {
                bt.setText(R.string.phone);
                bt.setImageResource(R.drawable.phone_on_button);
            }
        }
    }
}
