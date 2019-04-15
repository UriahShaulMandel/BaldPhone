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

import android.util.SparseIntArray;

import com.bald.uriah.baldphone.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

//Large object instead of subclasses.
//Will be improved in the future, with music reminders , with custom colored pills and more

/**
 * using this old java getters and setters because Room requires that.
 * see {@link Entity}
 */
@Entity
public class Reminder {
    public static final String REMINDER_KEY_VIA_INTENTS = "REMINDER_KEY_VIA_INTENTS";
    @Ignore
    public static final int TYPE_PILL = 0, TYPE_REGULAR = 1, TYPE_PICTURE = 2, TYPE_BIRTHDAY = 3;
    @Ignore
    public static final int TIME_MORNING = 0, TIME_AFTERNOON = 1, TIME_EVENING = 2;
    @Ignore
    public static final int BINARY_RGB = 3, BINARY_PNG = 2, BINARY_M4A = 1, NULL = 0;
    @Ignore
    public static final SparseIntArray PILLS_TIME_NAMES = new SparseIntArray(3);

    static {
        PILLS_TIME_NAMES.append(Reminder.TIME_MORNING, R.string.morning);
        PILLS_TIME_NAMES.append(Reminder.TIME_AFTERNOON, R.string.afternoon);
        PILLS_TIME_NAMES.append(Reminder.TIME_EVENING, R.string.evening);
    }

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "textual_content")
    @Nullable
    private String textualContent;
    @ColumnInfo(name = "binary_content_type")
    @BinaryType
    private int binaryContentType;
    @ColumnInfo(name = "binary_content")
    @Nullable
    private byte[] binaryContent;
    @Time
    @ColumnInfo(name = "starting_time")
    private int startingTime = Reminder.TIME_MORNING;
    @ColumnInfo(name = "hour")
    private int hour;
    @ColumnInfo(name = "minute")
    private int minute;
    @ColumnInfo(name = "days")
    private int days = -1;
    @ColumnInfo(name = "type")
    @Type
    private int reminderType;

    public Reminder() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public String getTextualContent() {
        return textualContent;
    }

    public void setTextualContent(@Nullable String textualContent) {
        this.textualContent = textualContent;
    }

    @BinaryType
    public int getBinaryContentType() {
        return binaryContentType;
    }

    public void setBinaryContentType(@BinaryType int binaryContentType) {
        this.binaryContentType = binaryContentType;
    }

    public byte[] getBinaryContent() {
        return binaryContent;
    }

    public void setBinaryContent(@Nullable byte[] binaryContent) {
        this.binaryContent = binaryContent;
    }

    @Time
    public int getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(@Time int startingTime) {
        this.startingTime = startingTime;
    }

    @Type
    public int getReminderType() {
        return reminderType;
    }

    public void setReminderType(@Type int reminderType) {
        this.reminderType = reminderType;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Ignore
    @StringRes
    public int getTimeAsStringRes() {
        return PILLS_TIME_NAMES.get(startingTime);
    }

    @IntDef({TYPE_PILL, TYPE_REGULAR, TYPE_BIRTHDAY, TYPE_PICTURE})
    @Retention(value = RetentionPolicy.SOURCE)
    @interface Type {
    }

    @IntDef({TIME_MORNING, TIME_AFTERNOON, TIME_EVENING})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface Time {
    }

    @IntDef({BINARY_RGB, BINARY_PNG, BINARY_M4A, NULL})
    @Retention(value = RetentionPolicy.SOURCE)
    @interface BinaryType {
    }


}
