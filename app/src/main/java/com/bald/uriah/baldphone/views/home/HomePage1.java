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

package com.bald.uriah.baldphone.views.home;

import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.AppsActivity;
import com.bald.uriah.baldphone.activities.DialerActivity;
import com.bald.uriah.baldphone.activities.HomeScreenActivity;
import com.bald.uriah.baldphone.activities.RecentActivity;
import com.bald.uriah.baldphone.activities.alarms.AlarmsActivity;
import com.bald.uriah.baldphone.activities.contacts.ContactsActivity;
import com.bald.uriah.baldphone.activities.media.PhotosActivity;
import com.bald.uriah.baldphone.activities.media.VideosActivity;
import com.bald.uriah.baldphone.activities.pills.PillsActivity;
import com.bald.uriah.baldphone.databases.apps.App;
import com.bald.uriah.baldphone.databases.apps.AppsDatabase;
import com.bald.uriah.baldphone.services.NotificationListenerService;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.FirstPageAppIcon;

import java.util.HashSet;
import java.util.Set;

import static com.bald.uriah.baldphone.services.NotificationListenerService.*;

public class HomePage1 extends HomeView {
    public static final String TAG = HomePage1.class.getSimpleName();
    private final static String WHATSAPP_PACKAGE_NAME = "com.whatsapp";
    private View view;
    private FirstPageAppIcon bt_clock, bt_camera, bt_videos, bt_assistant, bt_messages, bt_photos, bt_contacts, bt_dialer, bt_whatsapp, bt_apps, bt_reminders, bt_recent;
    private PackageManager packageManager;
    private boolean registered = false;
    private App app;

    public HomePage1(@NonNull HomeScreenActivity homeScreen) {
        super(homeScreen);
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Set<String> packagesSet = new HashSet<>(intent.getStringArrayListExtra("packages"));
            bt_messages.setBadgeVisibility(packagesSet.contains(Telephony.Sms.getDefaultSmsPackage(homeScreen)));
            if (app == null) {
                bt_whatsapp.setBadgeVisibility(packagesSet.contains(D.WHATSAPP_PACKAGE_NAME));
            } else
                bt_whatsapp.setBadgeVisibility(packagesSet.contains(ComponentName.unflattenFromString(app.getFlattenComponentName()).getPackageName()));
        }
    };

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.fragment_home_page1, container, false);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            view.findViewById(R.id.clock).setVisibility(View.GONE);

        packageManager = homeScreen.getPackageManager();
        attachXml();
        genOnClickListeners();
        return view;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!registered) {
            LocalBroadcastManager.getInstance(homeScreen)
                    .registerReceiver(notificationReceiver,
                            new IntentFilter(NotificationListenerService.HOME_SCREEN_ACTIVITY_BROADCAST));
            registered = true;
            LocalBroadcastManager.getInstance(homeScreen).sendBroadcast(
                    new Intent(ACTION_REGISTER_ACTIVITY)
                            .putExtra(KEY_EXTRA_ACTIVITY, NOTIFICATIONS_HOME_SCREEN));
        }
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (registered) {
            LocalBroadcastManager.getInstance(homeScreen)
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
        PackageManager localPackageManager = homeScreen.getPackageManager();
        final Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        final ActivityInfo activity = localPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo;
        final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .setComponent(name);

    }

    private void genOnClickListeners() {
        final SharedPreferences sharedPreferences = BPrefs.get(homeScreen);
        if (sharedPreferences.contains(BPrefs.CUSTOM_APP_KEY)) {
            app = AppsDatabase.getInstance(homeScreen).appsDatabaseDao().findByFlattenComponentName(sharedPreferences.getString(BPrefs.CUSTOM_APP_KEY, null));
            if (app == null)
                sharedPreferences.edit().remove(BPrefs.CUSTOM_APP_KEY).apply();
        } else
            app = null;

        if (app == null) {
            if (S.isPackageInstalled(homeScreen, WHATSAPP_PACKAGE_NAME))
                bt_whatsapp.setOnClickListener(v -> homeScreen.startActivity(
                        new Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_LAUNCHER)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)// | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                .setComponent(new ComponentName(WHATSAPP_PACKAGE_NAME, "com.whatsapp.Main"))));
            else
                bt_whatsapp.setOnClickListener(v -> {
                    try {
                        homeScreen.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + WHATSAPP_PACKAGE_NAME)));
                    } catch (android.content.ActivityNotFoundException e) {
                        homeScreen.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + WHATSAPP_PACKAGE_NAME)));
                    }
                });
        } else {
            bt_whatsapp.setText(app.getLabel());
            bt_whatsapp.setImageBitmap(S.byteArrayToBitmap(app.getIcon()));
            bt_whatsapp.setOnClickListener(v -> S.startComponentName(homeScreen, ComponentName.unflattenFromString(app.getFlattenComponentName())));
        }

        bt_apps.setOnClickListener(v -> {
            if (!homeScreen.finishedUpdatingApps)
                homeScreen.launchAppsActivity = true;
            else
                homeScreen.startActivity(new Intent(HomePage1.this.homeScreen, AppsActivity.class));
        });

        bt_clock.setOnClickListener(v -> homeScreen.startActivity(new Intent(homeScreen, AlarmsActivity.class)));
        bt_contacts.setOnClickListener(v -> homeScreen.startActivity(new Intent(HomePage1.this.homeScreen, ContactsActivity.class)));
        bt_dialer.setOnClickListener(v -> homeScreen.startActivity(new Intent(homeScreen, DialerActivity.class)));
        bt_recent.setOnClickListener(v -> homeScreen.startActivity(new Intent(homeScreen, RecentActivity.class)));
        bt_reminders.setOnClickListener(v -> homeScreen.startActivity(new Intent(homeScreen, PillsActivity.class)));
        bt_camera.setOnClickListener(v -> homeScreen.startActivity(getCameraIntent()));
        bt_photos.setOnClickListener(v -> homeScreen.startActivity(new Intent(homeScreen, PhotosActivity.class)));
        bt_videos.setOnClickListener(v -> homeScreen.startActivity(new Intent(homeScreen, VideosActivity.class)));
        bt_messages.setOnClickListener(v -> {
            try {
                homeScreen.startActivity(packageManager.getLaunchIntentForPackage(Telephony.Sms.getDefaultSmsPackage(homeScreen)));
            } catch (Exception e) {
                BaldToast.from(homeScreen).setType(BaldToast.TYPE_ERROR).setText(R.string.an_error_has_occurred).show();
            }
        });
        bt_assistant.setOnClickListener(v -> {
            try {
                homeScreen.startActivity(new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                BaldToast.from(homeScreen).setType(BaldToast.TYPE_ERROR).setText(R.string.your_phone_doesnt_have_assistant_installed).show();
            }
        });
    }

}
