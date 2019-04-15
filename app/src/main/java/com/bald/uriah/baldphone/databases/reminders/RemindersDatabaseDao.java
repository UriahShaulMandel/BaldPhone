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

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface RemindersDatabaseDao {

    @Query("SELECT * FROM Reminder")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM Reminder WHERE `id` = :id LIMIT 1")
    Reminder getById(int id);

    @Query("SELECT * FROM Reminder ORDER BY starting_time ASC")
    List<Reminder> getAllRemindersOrderedByTime();


    @Query("DELETE FROM Reminder WHERE id = :id")
    void removeReminder(int id);

    @Query("DELETE FROM Reminder WHERE id IN (:ids)")
    void removeReminders(int... ids);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Reminder... reminders);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Reminder reminders);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long replace(Reminder reminder);

    @Query("SELECT COUNT(*) FROM Reminder")
    int getNumberOfRows();

    @Query("DELETE FROM Reminder")
    void deleteAll();
}