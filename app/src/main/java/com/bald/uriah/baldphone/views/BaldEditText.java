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

package com.bald.uriah.baldphone.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatEditText;

import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.D;

/**
 * This class isn't the same as {@link BaldButton} so be careful
 */
public class BaldEditText extends AppCompatEditText implements View.OnLongClickListener {
    private final SharedPreferences sharedPreferences;
    private final boolean vibrationFeedback;
    private final Vibrator vibrator;

    public BaldEditText(Context context) {
        super(context);
        this.sharedPreferences = context.getSharedPreferences(BPrefs.KEY, Context.MODE_PRIVATE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        super.setOnLongClickListener(this);
    }

    public BaldEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.sharedPreferences = context.getSharedPreferences(BPrefs.KEY, Context.MODE_PRIVATE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        super.setOnLongClickListener(this);
    }

    public BaldEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.sharedPreferences = context.getSharedPreferences(BPrefs.KEY, Context.MODE_PRIVATE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        super.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        if (vibrationFeedback)
            vibrator.vibrate(D.vibetime);

        if (requestFocus()) {
            final CharSequence charSequence = getText();
            if (charSequence != null) {
                setSelection(charSequence.length());
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
            }
        }
        return true;
    }
}