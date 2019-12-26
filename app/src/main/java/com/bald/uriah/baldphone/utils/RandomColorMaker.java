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

import android.graphics.Color;
import android.util.SparseIntArray;

import androidx.annotation.ColorInt;

import java.util.Random;

public class RandomColorMaker {
    private SparseIntArray hashsToColors = new SparseIntArray();
    private Random random = new Random();
    private boolean bright;

    public RandomColorMaker(int backgroundColor) {
        this.bright = !isColorBright(backgroundColor);
    }

    private static boolean isColorBright(@ColorInt int colorInt) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt), hsv);
        return hsv[2] > 0.5f;
    }

    @ColorInt
    public int generateColor(int hash) {
        @ColorInt int color;
        color = hashsToColors.get(hash, -1);
        if (color == -1) {
            color = Color.HSVToColor(new float[]{random.nextFloat() * 360, 0.65f, bright ? 0.3f * random.nextFloat() : 0.9f - 0.3f * random.nextFloat()});
            hashsToColors.append(hash, color);
        }
        return color;
    }
}
