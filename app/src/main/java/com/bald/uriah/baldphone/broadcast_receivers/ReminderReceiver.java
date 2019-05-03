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
import com.bald.uriah.baldphone.activities.pills.PillScreenActivity;
import com.bald.uriah.baldphone.databases.reminders.Reminder;
import com.bald.uriah.baldphone.databases.reminders.ReminderScheduler;
import com.bald.uriah.baldphone.databases.reminders.RemindersDatabase;

/**
 * the middle man between the {@link ReminderScheduler} and {@link PillScreenActivity}.
 * the reason for having this is the wake lock the system creates for broadcast receivers.
 */
public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = ReminderReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final int id = intent.getIntExtra(Reminder.REMINDER_KEY_VIA_INTENTS, -1);
        if (id == -1) throw new IllegalArgumentException("set reminder id!");
        final Reminder reminder = RemindersDatabase.getInstance(context).remindersDatabaseDao().getById(id);
        if (reminder == null) {
            Log.e(TAG, "onReceive: RemindersDatabase.getInstance(context).remindersDatabaseDao().getByKey(id) == null");
            return;
        }

        final Context appContext = context.getApplicationContext();
        appContext.startActivity(new Intent(appContext, PillScreenActivity.class)
                .putExtra(Reminder.REMINDER_KEY_VIA_INTENTS, id)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }
}
