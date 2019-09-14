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
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;

public class BatteryView extends BaldImageButton {
    public int percentage;

    public BatteryView(Context context) {
        super(context);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLevel(int level, boolean charged) {
        percentage = level;
        @DrawableRes int drawableRes = R.drawable.battery_unknown_on_background;
        if (charged) {
            if (level < 20) {
                drawableRes = R.drawable.battery_20_c_on_background;
            } else if (level < 30) {
                drawableRes = R.drawable.battery_30_c_on_background;
            } else if (level < 50) {
                drawableRes = R.drawable.battery_50_c_on_background;
            } else if (level < 60) {
                drawableRes = R.drawable.battery_60_c_on_background;
            } else if (level < 80) {
                drawableRes = R.drawable.battery_80_c_on_background;
            } else if (level < 90) {
                drawableRes = R.drawable.battery_90_c_on_background;
            } else if (level < 100) {
                drawableRes = R.drawable.battery_100_charging;
            } else
                drawableRes = R.drawable.battery_full_on_background;
        } else {
            if (level < 20) {
                drawableRes = R.drawable.battery_20_on_background;
            } else if (level < 30) {
                drawableRes = R.drawable.battery_30_on_background;
            } else if (level < 50) {
                drawableRes = R.drawable.battery_50_on_background;
            } else if (level < 60) {
                drawableRes = R.drawable.battery_60_on_background;
            } else if (level < 80) {
                drawableRes = R.drawable.battery_80_on_background;
            } else if (level < 90) {
                drawableRes = R.drawable.battery_90_on_background;
            } else if (level <= 100) {
                drawableRes = R.drawable.battery_full_on_background;
            }

        }
        setImageResource(drawableRes);
    }
}
