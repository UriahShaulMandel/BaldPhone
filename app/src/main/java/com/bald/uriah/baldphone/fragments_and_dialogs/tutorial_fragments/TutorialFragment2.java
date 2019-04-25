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

package com.bald.uriah.baldphone.fragments_and_dialogs.tutorial_fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;

public class TutorialFragment2 extends TutorialFragment {
    private Vibrator vibrator;
    private TextView[] presses = new TextView[3];
    private TextView[] selected = new TextView[3];

    private SharedPreferences sharedPreferences;
    @ColorInt
    private int textColorOnSelected, textColorOnButton;
    private int longPressesFlag = 2;//2 is most 1 is medium 0 is regular

    @Override
    protected void actualSetup() {
        sharedPreferences = BPrefs.get(getContext());
        longPressesFlag = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_KEY, BPrefs.LONG_PRESSES_DEFAULT_VALUE) ? sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_SHORTER_KEY, BPrefs.LONG_PRESSES_SHORTER_DEFAULT_VALUE) ? 1 : 2 : 0;
        applyDuoToFlag();

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        final TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.bald_text_on_selected, typedValue, true);
        textColorOnSelected = typedValue.data;
        theme.resolveAttribute(R.attr.bald_text_on_button, typedValue, true);
        textColorOnButton = typedValue.data;

        applyDuoToFlag();

        presses[2].setOnLongClickListener((v) -> {
            clickedEffect((TextView) v);
            vibrator.vibrate(D.vibetime);
            return true;
        });
        presses[2].setOnClickListener((v) -> BaldToast.from(v.getContext()).setText(R.string.press_longer).show());
        presses[1].setOnClickListener(v -> clickedEffect((TextView) v));
        presses[0].setOnClickListener(v -> clickedEffect((TextView) v));
    }

    @Override
    protected void attachXml() {

        presses[2] = root.findViewById(R.id.long_presses);
        presses[1] = root.findViewById(R.id.medium_presses);
        presses[0] = root.findViewById(R.id.short_presses);

        for (int i = 0; i < presses.length; i++)
            presses[i].setTag(i);

        selected[2] = root.findViewById(R.id.long_presses_selected);
        selected[1] = root.findViewById(R.id.medium_presses_selected);
        selected[0] = root.findViewById(R.id.short_presses_selected);
    }

    private void clickedEffect(TextView v) {
        longPressesFlag = (int) v.getTag();
        applyDuoToFlag();
        sharedPreferences.edit()
                .putBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, longPressesFlag > 0)
                .putBoolean(BPrefs.LONG_PRESSES_KEY, longPressesFlag > 0)
                .putBoolean(BPrefs.LONG_PRESSES_SHORTER_KEY, longPressesFlag == 1)
                .putBoolean(BPrefs.TOUCH_NOT_HARD_KEY, longPressesFlag == 0)
                .apply();
        Activity activity = getActivity();
        if (activity != null) {
            activity.recreate();
        }

    }

    private void applyDuoToFlag() {
        for (int i = 0; i < presses.length; i++) {
            selected[i].setVisibility(longPressesFlag == i ? View.VISIBLE : View.INVISIBLE);
            presses[i].setBackgroundResource(longPressesFlag == i ? R.drawable.btn_selected : R.drawable.style_for_buttons);
            presses[i].setTextColor(longPressesFlag == i ? textColorOnSelected : textColorOnButton);
        }

    }

    @Override
    protected int layoutRes() {
        return R.layout.tutorial_fragment_2;
    }
}
