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
import android.os.Vibrator;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.core.GeneralConstants;

public class MediumBaldButtonForTutorial extends androidx.appcompat.widget.AppCompatTextView implements BaldButtonInterface {
    private Vibrator vibrator;
    private OnClickListener onClickListener;

    public MediumBaldButtonForTutorial(Context context) {
        super(context);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public MediumBaldButtonForTutorial(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public MediumBaldButtonForTutorial(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void baldPerformClick() {
        if (onClickListener != null)
            onClickListener.onClick(this);
    }

    @Override
    public void vibrate() {
        vibrator.vibrate(GeneralConstants.vibetime);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
        super.setOnClickListener(GeneralConstants.EMPTY_CLICK_LISTENER);
        super.setOnTouchListener(new BaldButtonTouchListener(this));

    }
}