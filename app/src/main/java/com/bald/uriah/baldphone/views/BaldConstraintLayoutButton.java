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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;

/**
 * Simple Button, extends {@link ConstraintLayout}; adapted to App settings.
 * EXACTLY the same code as {@link BaldButton}, but extends {@link ConstraintLayout} instead.
 * for more details, head to {@link BaldButton}
 */
public class BaldConstraintLayoutButton extends ConstraintLayout implements BaldButtonInterface, View.OnLongClickListener, View.OnClickListener {
    private OnClickListener onClickListener;
    private BaldButtonTouchListener baldButtonTouchListener;
    private final SharedPreferences sharedPreferences;
    private final boolean longPresses, vibrationFeedback, longPressesShorter;
    private final Vibrator vibrator;
    private final BaldToast longer;


    @SuppressLint("ClickableViewAccessibility")
    public BaldConstraintLayoutButton(Context context) {
        super(context);
        this.sharedPreferences = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE);
        this.longPresses = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_KEY, BPrefs.LONG_PRESSES_DEFAULT_VALUE);
        this.longPressesShorter = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_SHORTER_KEY, BPrefs.LONG_PRESSES_SHORTER_DEFAULT_VALUE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        longer = longPresses ? BaldToast.from(context).setText(context.getText(R.string.press_longer)).setType(BaldToast.TYPE_DEFAULT).setLength(0).build() : null;
        if (longPresses)
            if (longPressesShorter) {
                baldButtonTouchListener = new BaldButtonTouchListener(this);
                super.setOnTouchListener(baldButtonTouchListener);
                super.setOnClickListener(D.EMPTY_CLICK_LISTENER);
            } else {
                super.setOnLongClickListener(this);
                super.setOnClickListener(this);
            }
        else
            super.setOnClickListener(this);

    }

    @SuppressLint("ClickableViewAccessibility")
    public BaldConstraintLayoutButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.sharedPreferences = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE);
        this.longPresses = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_KEY, BPrefs.LONG_PRESSES_DEFAULT_VALUE);
        this.longPressesShorter = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_SHORTER_KEY, BPrefs.LONG_PRESSES_SHORTER_DEFAULT_VALUE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        longer = longPresses ? BaldToast.from(context).setText(context.getText(R.string.press_longer)).setType(BaldToast.TYPE_DEFAULT).setLength(0).build() : null;
        if (longPresses)
            if (longPressesShorter) {
                baldButtonTouchListener = new BaldButtonTouchListener(this);
                super.setOnTouchListener(baldButtonTouchListener);
                super.setOnClickListener(D.EMPTY_CLICK_LISTENER);
            } else {
                super.setOnLongClickListener(this);
                super.setOnClickListener(this);
            }
        else
            super.setOnClickListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    public BaldConstraintLayoutButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.sharedPreferences = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE);
        this.longPresses = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_KEY, BPrefs.LONG_PRESSES_DEFAULT_VALUE);
        this.longPressesShorter = sharedPreferences.getBoolean(BPrefs.LONG_PRESSES_SHORTER_KEY, BPrefs.LONG_PRESSES_SHORTER_DEFAULT_VALUE);
        this.vibrationFeedback = sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE);
        this.vibrator = this.vibrationFeedback ? (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) : null;
        longer = longPresses ? BaldToast.from(context).setText(context.getText(R.string.press_longer)).setType(BaldToast.TYPE_DEFAULT).setLength(0).build() : null;
        if (longPresses)
            if (longPressesShorter) {
                baldButtonTouchListener = new BaldButtonTouchListener(this);
                super.setOnTouchListener(baldButtonTouchListener);
                super.setOnClickListener(D.EMPTY_CLICK_LISTENER);
            } else {
                super.setOnLongClickListener(this);
                super.setOnClickListener(this);
            }
        else
            super.setOnClickListener(this);
    }



    @Override
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * use {@link BaldButton#setOnLongClickListener(android.view.View.OnLongClickListener)} instead
     */
    @Deprecated
    @Override
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        throw new RuntimeException("use setOnClickListener(View.OnClickListener onClickListener) instead");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setOnTouchListener(OnTouchListener l) {
        if (longPressesShorter)
            baldButtonTouchListener.addListener(l);
        else
            super.setOnTouchListener(l);
    }

    @Override
    public void onClick(View v) {
        if (longPresses) {
            longer.show();
        } else {
            vibrate();
            if (onClickListener != null)
                onClickListener.onClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longPresses) {
            vibrate();

            if (onClickListener != null)
                onClickListener.onClick(v);
            return true;
        }
        return false;

    }

    @Override
    public void baldPerformClick() {
        if (onClickListener != null)
            onClickListener.onClick(this);    }

    @Override
    public void vibrate() {
        if (vibrationFeedback)
            vibrator.vibrate(D.vibetime);
    }
}
