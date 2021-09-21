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

package com.bald.uriah.baldphone.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseIntArray;

import androidx.annotation.StyleRes;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.apps.pills.Reminder;
import com.bald.uriah.baldphone.utils.DateTimeUtils;

public final class BPrefs {
    public static final String KEY = "baldPrefs";
    public static final String TOUCH_NOT_HARD_KEY = "TOUCH_NOT_HARD_KEY";
    public static final boolean TOUCH_NOT_HARD_DEFAULT_VALUE = false;

    public static final String RIGHT_HANDED_KEY = "RIGHT_HANDED_KEY";
    public static final boolean RIGHT_HANDED_DEFAULT_VALUE = true;

    public static final String CUSTOM_APP_KEY = "CUSTOM_APP_KEY"; // Backward Compatible
    public static final String CUSTOM_RECENTS_KEY = "CUSTOM_RECENTS_KEY";
    public static final String CUSTOM_DIALER_KEY = "CUSTOM_DIALER_KEY";
    public static final String CUSTOM_CONTACTS_KEY = "CUSTOM_CONTACTS_KEY";
    public static final String CUSTOM_ASSISTANT_KEY = "CUSTOM_ASSISTANT_KEY";
    public static final String CUSTOM_MESSAGES_KEY = "CUSTOM_MESSAGES_KEY";
    public static final String CUSTOM_PHOTOS_KEY = "CUSTOM_PHOTOS_KEY";
    public static final String CUSTOM_CAMERA_KEY = "CUSTOM_CAMERA_KEY";
    public static final String CUSTOM_VIDEOS_KEY = "CUSTOM_VIDEOS_KEY";
    public static final String CUSTOM_PILLS_KEY = "CUSTOM_PILLS_KEY";
    public static final String CUSTOM_APPS_KEY = "CUSTOM_APPS_KEY";
    public static final String CUSTOM_ALARMS_KEY = "CUSTOM_ALARMS_KEY";

    public static final String LONG_PRESSES_KEY = "LONG_PRESSES_KEY";
    public static final boolean LONG_PRESSES_DEFAULT_VALUE = true;

    public static final String TEST_KEY = "TEST_KEY";
    public static final boolean TEST_DEFAULT_VALUE = false;

    public static final String LONG_PRESSES_SHORTER_KEY = "LONG_PRESSES_SHORTER_KEY";
    public static final boolean LONG_PRESSES_SHORTER_DEFAULT_VALUE = true;

    public static final String STATUS_BAR_KEY = "STATUS_BAR_KEY";
    public static final int STATUS_BAR_DEFAULT_VALUE = 0;

    public static final String VIBRATION_FEEDBACK_KEY = "VIBRATION_FEEDBACK_KEY";
    public static final boolean VIBRATION_FEEDBACK_DEFAULT_VALUE = true;

    public static final String THEME_KEY = "THEME_KEY";
    public static final int THEME_DEFAULT_VALUE = Themes.ADAPTIVE;

    public static final String NOTE_VISIBLE_KEY = "NOTE_VISIBLE_KEY";
    public static final boolean NOTE_VISIBLE_DEFAULT_VALUE = true;

    public static final String EMERGENCY_BUTTON_VISIBLE_KEY = "EMERGENCY_BUTTON_VISIBLE_KEY";
    public static final boolean EMERGENCY_BUTTON_VISIBLE_DEFAULT_VALUE = true;

    public static final String LOW_BATTERY_ALERT_KEY = "LOW_BATTERY_ALERT_KEY";
    public static final boolean LOW_BATTERY_ALERT_DEFAULT_VALUE = true;

    public static final String DIALER_SOUNDS_KEY = "DIALER_SOUNDS_KEY";
    public static final boolean DIALER_SOUNDS_DEFAULT_VALUE = true;

    public static final String DUAL_SIM_KEY = "DUAL_SIM_KEY";
    public static final boolean DUAL_SIM_DEFAULT_VALUE = false; // True means show options


    public static final String NOTE_KEY = "NOTE_KEY";

    public static final String AFTER_TUTORIAL_KEY = "AFTER_TUTORIAL_KEY";

    public static final String PAGE_TRANSFORMERS_KEY = "pageTransformersKey";
    public static final int PAGE_TRANSFORMERS_DEFAULT_VALUE = 0;

    public static final String USE_ACCIDENTAL_GUARD_KEY = "USE_ACCIDENTAL_GUARD_KEY";
    public static final boolean USE_ACCIDENTAL_GUARD_DEFAULT_VALUE = true;

    public static final String CRASH_REPORTS_KEY = "CRASH_REPORTS_KEY";
    public static final boolean CRASH_REPORTS_DEFAULT_VALUE = true;

    public static final String APPS_ONE_GRID_KEY = "APPS_ONE_GRID_KEY";
    public static final boolean APPS_ONE_GRID_DEFAULT_VALUE = false;

    public static final String COLORFUL_KEY = "COLORFUL_KEY";
    public static final boolean COLORFUL_DEFAULT_VALUE = false;

    public static final String LAST_CRASH_KEY = "LAST_CRASH_KEY";
    public static final long LAST_CRASH_TIME_OK = 12 * DateTimeUtils.SECOND;

    public static final String LAST_APK_VERSION_KEY = "LAST_APK_VERSION_KEY";

    public static final String LAST_UPDATE_ASKED_VERSION_KEY = "LAST_UPDATE_ASKED_VERSION_KEY";

    public static final String UUID_KEY = "UUID_KEY";

    public static final String LAST_DOWNLOAD_MANAGER_REQUEST_ID = "LAST_DOWNLOAD_MANAGER_REQUEST_ID";
    public static final String LAST_DOWNLOAD_MANAGER_REQUEST_VERSION_NUMBER = "LAST_DOWNLOAD_MANAGER_REQUEST_VERSION_NUMBER";

    public static final String HOUR_KEY_ = "HOUR_KEY_";
    public static final String MINUTE_KEY_ = "MINUTE_KEY_";

    public static final String ALARM_VOLUME_KEY = "ALARM_VOLUME_KEY";
    public static final int ALARM_VOLUME_DEFAULT_VALUE = 4;
    public static final SparseIntArray PILLS_HOUR_DEFAULTS = new SparseIntArray(3);
    public static final SparseIntArray PILLS_MINUTE_DEFAULTS = new SparseIntArray(3);

    static {
        PILLS_HOUR_DEFAULTS.append(Reminder.TIME_MORNING, 7);
        PILLS_HOUR_DEFAULTS.append(Reminder.TIME_AFTERNOON, 12);
        PILLS_HOUR_DEFAULTS.append(Reminder.TIME_EVENING, 19);

        PILLS_MINUTE_DEFAULTS.append(Reminder.TIME_MORNING, 30);
        PILLS_MINUTE_DEFAULTS.append(Reminder.TIME_AFTERNOON, 30);
        PILLS_MINUTE_DEFAULTS.append(Reminder.TIME_EVENING, 30);
    }

    public static void setHourAndMinute(Context context, @Reminder.Time int time, int hour, int minute) {
        get(context)
                .edit()
                .putInt(HOUR_KEY_ + time, hour)
                .putInt(MINUTE_KEY_ + time, minute)
                .apply();
    }

    public static int getHour(@Reminder.Time int time, Context context) {
        int hour = get(context).getInt(HOUR_KEY_ + time, -1);
        if (hour == -1)
            hour = PILLS_HOUR_DEFAULTS.get(time);
        return hour;
    }

    public static int getMinute(@Reminder.Time int time, Context context) {
        int minute = get(context).getInt(MINUTE_KEY_ + time, -1);
        if (minute == -1)
            minute = PILLS_MINUTE_DEFAULTS.get(time);
        return minute;
    }

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
    }

    public static class Themes {
        public static final int LIGHT = 0;
        public static final int ADAPTIVE = 1;
        public static final int DARK = 2;
        @StyleRes
        public static final int[] THEMES = new int[]{R.style.bald_light, -1, R.style.bald_dark, R.style.bald_skin};
    }
}
