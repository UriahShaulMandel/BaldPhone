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

package com.bald.uriah.baldphone.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bald.uriah.baldphone.activities.alarms.AlarmScreen;
import com.bald.uriah.baldphone.databases.alarms.Alarm;
import com.bald.uriah.baldphone.databases.alarms.AlarmScheduler;
import com.bald.uriah.baldphone.databases.alarms.AlarmsDatabase;

/**
 * the middle man between the {@link AlarmScheduler} and {@link AlarmScreen}.
 * the reason for having this is the wake lock the system creates for broadcast receivers.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final int key = intent.getIntExtra(Alarm.ALARM_KEY_VIA_INTENTS, -1);
        if (key == -1) throw new IllegalArgumentException("set alarm key!");
        final Alarm alarm = AlarmsDatabase.getInstance(context).alarmsDatabaseDao().getByKey(key);
        if (alarm == null) {
            Log.e(TAG, "onReceive: AlarmsDatabase.getInstance(context).alarmsDatabaseDao().getByKey(key) == null");
            return;
        } else if (!alarm.isEnabled()) {
            Log.e(TAG, "!alarm.isEnabled(), yet, most probably because of snooze...");
        }

        final Context appContext = context.getApplicationContext();
        appContext.startActivity(new Intent(appContext, AlarmScreen.class)
                .putExtra(Alarm.ALARM_KEY_VIA_INTENTS, key)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }
}
