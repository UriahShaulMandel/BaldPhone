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

package com.bald.uriah.baldphone.keyboard;

import android.annotation.SuppressLint;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.S;

@SuppressLint("ViewConstructor")
public class KeyboardPicker extends FrameLayout {
    public static final int LANGUAGE_ID = -1;

    public KeyboardPicker(BaldInputMethodService baldInputMethodService) {
        super(baldInputMethodService);
        final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(baldInputMethodService, S.getTheme(baldInputMethodService));
        final ConstraintLayout picker = (ConstraintLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.keyboard_language_picker, this, false);
        picker.findViewById(R.id.bt_hebrew).setOnClickListener(v -> baldInputMethodService.changeLanguage(HebrewKeyboard.LANGUAGE_ID));
        picker.findViewById(R.id.bt_english).setOnClickListener(v -> baldInputMethodService.changeLanguage(EnglishKeyboard.LANGUAGE_ID));
        addView(picker);
    }
}
