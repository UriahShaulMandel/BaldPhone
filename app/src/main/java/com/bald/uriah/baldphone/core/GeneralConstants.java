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

import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import com.bald.uriah.baldphone.R;

public class GeneralConstants {
    public static final int vibetime = 100;
    public static final View.OnClickListener longer = v -> Toast.makeText(v.getContext(), R.string.press_longer, Toast.LENGTH_LONG).show();
    public static final View.OnClickListener EMPTY_CLICK_LISTENER = v -> {
    };
    @ColorInt
    public static final int DEFAULT_STATUS_BAR_COLOR = Color.BLACK;

    //nope.
    private GeneralConstants() {
    }

}
