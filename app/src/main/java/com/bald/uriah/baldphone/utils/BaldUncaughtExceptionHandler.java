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

package com.bald.uriah.baldphone.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.bald.uriah.baldphone.core.CrashActivity;
import com.bald.uriah.baldphone.core.BPrefs;

import org.acra.ACRA;

public class BaldUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultUEH;

    public BaldUncaughtExceptionHandler(Context context, Thread.UncaughtExceptionHandler defaultUEH) {
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
        baldPrefs.edit().putLong(BPrefs.LAST_CRASH_KEY, currentTime).commit(); // commit and not apply because of System.exit(2)
        S.logImportant("BaldPhone CRASHED!");
        if (baldPrefs.getBoolean(BPrefs.CRASH_REPORTS_KEY, BPrefs.CRASH_REPORTS_DEFAULT_VALUE))
            ACRA.getErrorReporter().handleException(e);

        final PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        19337,
                        new Intent(context, CrashActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK),
                        PendingIntent.FLAG_ONE_SHOT
                );

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 300 * DateTimeUtils.MILLISECOND, pendingIntent);

        Runtime.getRuntime().exit(2);
    }
}