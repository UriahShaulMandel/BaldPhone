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

package com.bald.uriah.baldphone;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.bald.uriah.baldphone.activities.CrashActivity;
import com.bald.uriah.baldphone.activities.UpdatesActivity;
import com.bald.uriah.baldphone.databases.alarms.AlarmScheduler;
import com.bald.uriah.baldphone.databases.reminders.ReminderScheduler;
import com.bald.uriah.baldphone.services.NotificationListenerService;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

/**
 * base application class - onBoot makes onCreate run when devices opens.
 */

public class BaldPhone extends Application {
    private static final String TAG = BaldPhone.class.getSimpleName();

    @Override
    public void onCreate() {
        S.logImportant(TAG + " was started!");
        super.onCreate();
        AlarmScheduler.reStartAlarms(this);
        ReminderScheduler.reStartReminders(this);
        UpdatesActivity.removeUpdatesInfo(this);
        try {
            startService(new Intent(this, NotificationListenerService.class));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        S.logImportant("attachBaseContext was called! setting Acra and Thread.setDefaultUncaughtExceptionHandler");
        final CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
                .setBuildConfigClass(BuildConfig.class)
                .setReportFormat(StringFormat.JSON);
        builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder.class)
                .setUri(getString(R.string.tt_url))
                .setHttpMethod(HttpSender.Method.POST)
                .setEnabled(true);
        ACRA.init(this, builder);
        Thread.setDefaultUncaughtExceptionHandler(
                new BaldUncaughtExceptionHandler(
                        this,
                        Thread.getDefaultUncaughtExceptionHandler()));
    }


    private static class BaldUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        final Context context;
        final Thread.UncaughtExceptionHandler defaultUEH;

        BaldUncaughtExceptionHandler(Context context, Thread.UncaughtExceptionHandler defaultUEH) {
            this.context = context;
            this.defaultUEH = defaultUEH;
        }

        @SuppressLint("ApplySharedPref")
        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            final SharedPreferences baldPrefs = BPrefs.get(context);
            final long currentTime = System.currentTimeMillis();
            if (currentTime - baldPrefs.getLong(BPrefs.LAST_CRASH_KEY, -1) < BPrefs.LAST_CRASH_TIME_OK) {
                defaultUEH.uncaughtException(t, e);
                return;
            }
            baldPrefs.edit().putLong(BPrefs.LAST_CRASH_KEY, currentTime).commit();
            //NOPE - should be done immediately because of System.exit(2);
            S.logImportant("CRASHED!!");
            if (baldPrefs.getBoolean(BPrefs.CRASH_REPORTS_KEY, BPrefs.CRASH_REPORTS_DEFAULT_VALUE))
                ACRA.getErrorReporter().handleException(e);

            final PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    19337, new Intent(context, CrashActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_ONE_SHOT);
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    300 * D.MILLISECOND, pendingIntent);

            System.exit(2);
        }
    }
}
