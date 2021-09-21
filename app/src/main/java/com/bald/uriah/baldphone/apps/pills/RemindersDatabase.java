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

package com.bald.uriah.baldphone.apps.pills;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Reminder.class}, version = 1, exportSchema = false)
public abstract class RemindersDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static RemindersDatabase remindersDatabase = null;

    public static RemindersDatabase getInstance(Context context) {
        synchronized (LOCK) {
            if (remindersDatabase == null)
                remindersDatabase = Room.databaseBuilder(context.getApplicationContext(),
                        RemindersDatabase.class, "reminders")
                        .allowMainThreadQueries()
                        .build();
            return remindersDatabase;
        }
    }

    public abstract RemindersDatabaseDao remindersDatabaseDao();
}
