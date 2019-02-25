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

package com.bald.uriah.baldphone.databases.reminders;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Reminder.class}, version = 1, exportSchema = false)
public abstract class RemindersDatabase extends RoomDatabase {
    public abstract RemindersDatabaseDao remindersDatabaseDao();

    private static RemindersDatabase remindersDatabase = null;
    private static final Object LOCK = new Object();

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
}
