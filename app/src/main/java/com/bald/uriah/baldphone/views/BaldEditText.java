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

package com.bald.uriah.baldphone.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;

public class BaldEditText extends AppCompatEditText implements View.OnClickListener, View.OnLongClickListener {
    private OnClickListener onClickListener = v -> {
        if (requestFocus()) {
            setSelection(getText().length());
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);

        }
    };
    private final SharedPreferences sharedPreferences;
    private final boolean longPresses, vibrationFeedback;
    private final Vibrator vibrator;
    private final BaldToast longer;

    public BaldEditText(Context context) {
        super(context);
        this.sharedPreferences = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE);
        this.longPresses = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_KEY, BPrefs.LONG_PRESSES_DEFAULT_VALUE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        longer = longPresses ? BaldToast.from(context).setText(context.getText(R.string.press_longer)).setType(BaldToast.TYPE_DEFAULT).setLength(0).build() : null;
        super.setOnClickListener(this);
        super.setOnLongClickListener(this);
    }

    public BaldEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.sharedPreferences = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE);
        this.longPresses = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_KEY, BPrefs.LONG_PRESSES_DEFAULT_VALUE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        longer = longPresses ? BaldToast.from(context).setText(context.getText(R.string.press_longer)).setType(BaldToast.TYPE_DEFAULT).setLength(0).build() : null;
        super.setOnClickListener(this);
        super.setOnLongClickListener(this);
    }

    public BaldEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.sharedPreferences = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE);
        this.longPresses = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_KEY, BPrefs.LONG_PRESSES_DEFAULT_VALUE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        longer = longPresses ? BaldToast.from(context).setText(context.getText(R.string.press_longer)).setType(BaldToast.TYPE_DEFAULT).setLength(0).build() : null;
        super.setOnClickListener(this);
        super.setOnLongClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (longPresses) {
            longer.show();
        } else {
            if (vibrationFeedback)
                vibrator.vibrate(D.vibetime);

            if (onClickListener != null)
                onClickListener.onClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longPresses) {
            if (vibrationFeedback)
                vibrator.vibrate(D.vibetime);

            if (onClickListener != null)
                onClickListener.onClick(v);
            return true;
        }
        return false;

    }
}
