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

package com.bald.uriah.baldphone.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

import static com.bald.uriah.baldphone.utils.BPrefs.LONG_PRESSES_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.utils.BPrefs.LONG_PRESSES_KEY;
import static com.bald.uriah.baldphone.utils.BPrefs.NOTE_VISIBLE_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.utils.BPrefs.NOTE_VISIBLE_KEY;
import static com.bald.uriah.baldphone.utils.BPrefs.PAGE_TRANSFORMERS_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.utils.BPrefs.PAGE_TRANSFORMERS_KEY;
import static com.bald.uriah.baldphone.utils.BPrefs.TOUCH_NOT_HARD_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.utils.BPrefs.TOUCH_NOT_HARD_KEY;
import static com.bald.uriah.baldphone.utils.BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE;
import static com.bald.uriah.baldphone.utils.BPrefs.VIBRATION_FEEDBACK_KEY;

public class BaldPrefsUtils {
    private final int theme;
    private final boolean vibrationFeedback, touchNoHard, longPresses, notes;
    private final int swipingEffect;

    private BaldPrefsUtils(int theme, boolean vibrationFeedback, boolean touchNoHard, boolean longPresses, int swipingEffect, boolean notes) {
        this.theme = theme;
        this.vibrationFeedback = vibrationFeedback;
        this.touchNoHard = touchNoHard;
        this.longPresses = longPresses;
        this.swipingEffect = swipingEffect;
        this.notes = notes;
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
                        .getBoolean(NOTE_VISIBLE_KEY, NOTE_VISIBLE_DEFAULT_VALUE)
        );
    }

    public boolean hasChanged(Context context) {
        return !equals(newInstance(context));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BaldPrefsUtils that = (BaldPrefsUtils) o;
        return theme == that.theme &&
                vibrationFeedback == that.vibrationFeedback &&
                touchNoHard == that.touchNoHard &&
                longPresses == that.longPresses &&
                notes == that.notes &&
                swipingEffect == that.swipingEffect;
    }

    @Override
    public int hashCode() {
        return Objects.hash(theme, vibrationFeedback, touchNoHard, longPresses, notes, swipingEffect);
    }
}
