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

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_ALARMS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_APPS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_APP_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_ASSISTANT_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_CAMERA_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_CONTACTS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_DIALER_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_MESSAGES_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_PHOTOS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_PILLS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_RECENTS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.CUSTOM_VIDEOS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.EMERGENCY_BUTTON_VISIBLE_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.EMERGENCY_BUTTON_VISIBLE_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.LONG_PRESSES_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.LONG_PRESSES_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.LOW_BATTERY_ALERT_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.LOW_BATTERY_ALERT_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.NOTE_VISIBLE_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.NOTE_VISIBLE_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.PAGE_TRANSFORMERS_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.PAGE_TRANSFORMERS_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.STATUS_BAR_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.STATUS_BAR_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.TOUCH_NOT_HARD_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.TOUCH_NOT_HARD_KEY;
import static com.bald.uriah.baldphone.core.BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.core.BPrefs.VIBRATION_FEEDBACK_KEY;

import com.bald.uriah.baldphone.core.BPrefs;

public class BaldPrefsUtils {
    private final int theme;
    private final boolean vibrationFeedback, touchNoHard, longPresses, notes, lowBatteryAlert, sos;
    private final int swipingEffect, statusBar;
    private final String CUSTOM_APP;
    private final String CUSTOM_RECENTS;
    private final String CUSTOM_DIALER;
    private final String CUSTOM_CONTACTS;
    private final String CUSTOM_ASSISTANT;
    private final String CUSTOM_MESSAGES;
    private final String CUSTOM_PHOTOS;
    private final String CUSTOM_CAMERA;
    private final String CUSTOM_VIDEOS;
    private final String CUSTOM_PILLS;
    private final String CUSTOM_APPS;
    private final String CUSTOM_ALARMS;

    private BaldPrefsUtils(int theme, boolean vibrationFeedback, boolean touchNoHard, boolean longPresses, int swipingEffect, boolean notes, int statusBar, boolean lowBatteryAlert, boolean sos, String custom_app, String custom_recents, String custom_dialer, String custom_contacts, String custom_assistant, String custom_messages, String custom_photos, String custom_camera, String custom_videos, String custom_pills, String custom_apps, String custom_alarms) {
        this.theme = theme;
        this.vibrationFeedback = vibrationFeedback;
        this.touchNoHard = touchNoHard;
        this.longPresses = longPresses;
        this.swipingEffect = swipingEffect;
        this.notes = notes;
        this.statusBar = statusBar;
        this.lowBatteryAlert = lowBatteryAlert;
        this.sos = sos;
        CUSTOM_APP = custom_app;
        CUSTOM_RECENTS = custom_recents;
        CUSTOM_DIALER = custom_dialer;
        CUSTOM_CONTACTS = custom_contacts;
        CUSTOM_ASSISTANT = custom_assistant;
        CUSTOM_MESSAGES = custom_messages;
        CUSTOM_PHOTOS = custom_photos;
        CUSTOM_CAMERA = custom_camera;
        CUSTOM_VIDEOS = custom_videos;
        CUSTOM_PILLS = custom_pills;
        CUSTOM_APPS = custom_apps;
        CUSTOM_ALARMS = custom_alarms;
    }

    public static BaldPrefsUtils newInstance(Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(BPrefs.KEY, Context.MODE_PRIVATE);
        return new BaldPrefsUtils(
                S.getTheme(context),
                sharedPreferences
                        .getBoolean(VIBRATION_FEEDBACK_KEY, VIBRATION_FEEDBACK_DEFAULT_VALUE),
                sharedPreferences
                        .getBoolean(TOUCH_NOT_HARD_KEY, TOUCH_NOT_HARD_DEFAULT_VALUE),
                sharedPreferences
                        .getBoolean(LONG_PRESSES_KEY, LONG_PRESSES_DEFAULT_VALUE),
                sharedPreferences
                        .getInt(PAGE_TRANSFORMERS_KEY, PAGE_TRANSFORMERS_DEFAULT_VALUE),
                sharedPreferences
                        .getBoolean(NOTE_VISIBLE_KEY, NOTE_VISIBLE_DEFAULT_VALUE),
                sharedPreferences
                        .getInt(STATUS_BAR_KEY, STATUS_BAR_DEFAULT_VALUE),
                sharedPreferences.getBoolean(LOW_BATTERY_ALERT_KEY, LOW_BATTERY_ALERT_DEFAULT_VALUE),
                sharedPreferences.getBoolean(EMERGENCY_BUTTON_VISIBLE_KEY, EMERGENCY_BUTTON_VISIBLE_DEFAULT_VALUE),
                sharedPreferences.getString(CUSTOM_APP_KEY, null),
                sharedPreferences.getString(CUSTOM_RECENTS_KEY, null),
                sharedPreferences.getString(CUSTOM_DIALER_KEY, null),
                sharedPreferences.getString(CUSTOM_CONTACTS_KEY, null),
                sharedPreferences.getString(CUSTOM_ASSISTANT_KEY, null),
                sharedPreferences.getString(CUSTOM_MESSAGES_KEY, null),
                sharedPreferences.getString(CUSTOM_PHOTOS_KEY, null),
                sharedPreferences.getString(CUSTOM_CAMERA_KEY, null),
                sharedPreferences.getString(CUSTOM_VIDEOS_KEY, null),
                sharedPreferences.getString(CUSTOM_PILLS_KEY, null),
                sharedPreferences.getString(CUSTOM_APPS_KEY, null),
                sharedPreferences.getString(CUSTOM_ALARMS_KEY, null)
        );
    }

    public boolean hasChanged(Context context) {
        return !equals(newInstance(context));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaldPrefsUtils that = (BaldPrefsUtils) o;
        return theme == that.theme &&
                vibrationFeedback == that.vibrationFeedback &&
                touchNoHard == that.touchNoHard &&
                longPresses == that.longPresses &&
                notes == that.notes &&
                lowBatteryAlert == that.lowBatteryAlert &&
                sos == that.sos &&
                swipingEffect == that.swipingEffect &&
                statusBar == that.statusBar &&
                Objects.equals(CUSTOM_APP, that.CUSTOM_APP) &&
                Objects.equals(CUSTOM_RECENTS, that.CUSTOM_RECENTS) &&
                Objects.equals(CUSTOM_DIALER, that.CUSTOM_DIALER) &&
                Objects.equals(CUSTOM_CONTACTS, that.CUSTOM_CONTACTS) &&
                Objects.equals(CUSTOM_ASSISTANT, that.CUSTOM_ASSISTANT) &&
                Objects.equals(CUSTOM_MESSAGES, that.CUSTOM_MESSAGES) &&
                Objects.equals(CUSTOM_PHOTOS, that.CUSTOM_PHOTOS) &&
                Objects.equals(CUSTOM_CAMERA, that.CUSTOM_CAMERA) &&
                Objects.equals(CUSTOM_VIDEOS, that.CUSTOM_VIDEOS) &&
                Objects.equals(CUSTOM_PILLS, that.CUSTOM_PILLS) &&
                Objects.equals(CUSTOM_APPS, that.CUSTOM_APPS) &&
                Objects.equals(CUSTOM_ALARMS, that.CUSTOM_ALARMS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theme, vibrationFeedback, touchNoHard, longPresses, notes, lowBatteryAlert, sos, swipingEffect, statusBar, CUSTOM_APP, CUSTOM_RECENTS, CUSTOM_DIALER, CUSTOM_CONTACTS, CUSTOM_ASSISTANT, CUSTOM_MESSAGES, CUSTOM_PHOTOS, CUSTOM_CAMERA, CUSTOM_VIDEOS, CUSTOM_PILLS, CUSTOM_APPS, CUSTOM_ALARMS);
    }
}
