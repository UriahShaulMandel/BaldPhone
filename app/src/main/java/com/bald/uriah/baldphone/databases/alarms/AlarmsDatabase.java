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

package com.bald.uriah.baldphone.databases.alarms;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Alarm.class}, version = 1, exportSchema = false)

public abstract class AlarmsDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static AlarmsDatabase alarmsDatabase = null;

    public static AlarmsDatabase getInstance(Context context) {
        synchronized (LOCK) {
            if (alarmsDatabase == null)
                alarmsDatabase = Room.databaseBuilder(context.getApplicationContext(),
                        AlarmsDatabase.class, "alarmsbeta").allowMainThreadQueries().build();
            return alarmsDatabase;
        }
    }

    public abstract AlarmsDatabaseDao alarmsDatabaseDao();
}
