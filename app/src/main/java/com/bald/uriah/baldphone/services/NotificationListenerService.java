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

package com.bald.uriah.baldphone.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Objects;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {
    private static final String TAG = NotificationListenerService.class.getSimpleName();
    // BROADCASTS
    public static final String NOTIFICATIONS_ACTIVITY_BROADCAST = "NOTIFICATIONS_ACTIVITY_BROADCAST";
    public static final String HOME_SCREEN_ACTIVITY_BROADCAST = "HOME_SCREEN_ACTIVITY_BROADCAST";
    //    ACTIONS
    public static final String ACTION_REGISTER_ACTIVITY = "ACTION_REGISTER_ACTIVITY";
    // BROADCASTS
    public static final String ACTION_CLEAR = "ACTION_CLEAR";
    //    KEYS
    public static final String KEY_EXTRA_KEY = "KEY_EXTRA_KEY";
    //    ACTIONS
    public static final String KEY_EXTRA_NOTIFICATIONS = "KEY_EXTRA_NOTIFICATIONS";
    public static final String KEY_EXTRA_ACTIVITY = "KEY_EXTRA_ACTIVITY";
    public static final int
            NOTIFICATIONS_NONE = 0,
            NOTIFICATIONS_SOME = 2,
            NOTIFICATIONS_ALOT = 5;
    //    KEYS
    public static final int
            ACTIVITY_NONE = -1,
            NOTIFICATIONS_ACTIVITY = 1,
            NOTIFICATIONS_HOME_SCREEN = 2;
    // VARS
    @SupportedActivitys
    private int activity = ACTIVITY_NONE;
    private PackageManager packageManager;
    private boolean listening = false;
    // VARS
    private final BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = Objects.requireNonNull(intent.getAction());
            switch (action) {
                case ACTION_REGISTER_ACTIVITY:
                    activity = intent.getIntExtra(KEY_EXTRA_ACTIVITY, ACTIVITY_NONE);
                    sendBroadcastToActivity();
                    break;
                case ACTION_CLEAR:
                    cancelNotification(intent.getStringExtra(KEY_EXTRA_KEY));
                    break;
                default:
                    throw new AssertionError();
            }
        }
    };

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        sendBroadcastToActivity();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        sendBroadcastToActivity();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        listening = true;
        packageManager = getPackageManager();
        LocalBroadcastManager.getInstance(this).registerReceiver(listener,
                new IntentFilter(ACTION_REGISTER_ACTIVITY));
        LocalBroadcastManager.getInstance(this).registerReceiver(listener,
                new IntentFilter(ACTION_CLEAR));
        Log.e(TAG, "onListenerConnected: ");
    }

    private void sendBroadcastToActivity() {
        try {
            if (!listening)
                return;
            switch (activity) {
                case NotificationListenerService.NOTIFICATIONS_ACTIVITY:
                    sendBroadcastToNotificationsActivity();
                    break;
                case NotificationListenerService.NOTIFICATIONS_HOME_SCREEN:
                    sendBroadcastToHomeScreenActivity();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendBroadcastToNotificationsActivity() {
        try {
            final StatusBarNotification[] statusBarNotifications = getActiveNotifications();
            final Bundle[] bundlesToSend = new Bundle[statusBarNotifications.length];

            for (int i = 0, statusBarNotificationsLength = statusBarNotifications.length; i < statusBarNotificationsLength; i++) {
                final StatusBarNotification statusBarNotification = statusBarNotifications[i];
                final Notification notification = statusBarNotification.getNotification();
                final Bundle bundle = bundlesToSend[i] = new Bundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bundle.putParcelable("small_icon", notification.getSmallIcon());
                    bundle.putParcelable("large_icon", notification.getLargeIcon());
                } else {
                    bundle.putInt("small_icon", notification.icon);
                    bundle.putParcelable("large_icon", notification.largeIcon);
                }
                bundle.putCharSequence("title", notification.extras.getCharSequence(Notification.EXTRA_TITLE));
                bundle.putCharSequence("text", notification.extras.getCharSequence(Notification.EXTRA_TEXT));
                bundle.putLong("time_stamp", notification.when);
                final CharSequence packageName = statusBarNotification.getPackageName();
                bundle.putCharSequence("packageName", packageName);
                ApplicationInfo ai = null;
                try {
                    ai = packageManager.getApplicationInfo(String.valueOf(packageName), 0);
                } catch (final PackageManager.NameNotFoundException ignore) {
                }
                bundle.putCharSequence("app_name", (ai != null ? packageManager.getApplicationLabel(ai) : "(unknown)"));
                bundle.putParcelable("clear_intent", notification.deleteIntent);
                bundle.putParcelable("content_intent", notification.contentIntent);
                bundle.putBoolean("clearable", (notification.flags & Notification.FLAG_NO_CLEAR) == 0);
                bundle.putBoolean("summery", (notification.flags & Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY);
                bundle.putString(KEY_EXTRA_KEY, statusBarNotification.getKey());
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(NOTIFICATIONS_ACTIVITY_BROADCAST).putExtra(KEY_EXTRA_NOTIFICATIONS, bundlesToSend));
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendBroadcastToHomeScreenActivity() {
        try {
            final StatusBarNotification[] statusBarNotifications = getActiveNotifications();
            final ArrayList<String> packages = new ArrayList<>(statusBarNotifications.length);
            for (final StatusBarNotification statusBarNotification : statusBarNotifications) {
                packages.add(statusBarNotification.getPackageName());

            }
            final Intent intent = new Intent(HOME_SCREEN_ACTIVITY_BROADCAST)
                    .putExtra("amount", statusBarNotifications.length)
                    .putStringArrayListExtra("packages", packages);

            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @IntDef({ACTIVITY_NONE, NOTIFICATIONS_ACTIVITY, NOTIFICATIONS_HOME_SCREEN})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SupportedActivitys {
    }
}
