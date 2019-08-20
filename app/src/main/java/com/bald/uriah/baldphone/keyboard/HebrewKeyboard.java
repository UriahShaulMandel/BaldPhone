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
import android.content.Context;

import com.bald.uriah.baldphone.R;

@SuppressLint("ViewConstructor")

public class HebrewKeyboard extends BaldKeyboard {
    public static final int LANGUAGE_ID = 1;
    public static final char[] hebrewKeyboardCodes = new char[]{
            'ק', 'ר', 'א', 'ט', 'ו', 'ן', 'ם', 'פ', BACKSPACE,
            'ש', 'ד', 'ג', 'כ', 'ע', 'י', 'ח', 'ל', 'ך', 'ף',
            'ז', 'ס', 'ב', 'ה', 'נ', 'מ', 'צ', 'ת', 'ץ',
            NUMBERS, LANGUAGE, SPEECH_TO_TEXT, ' ', HIDE, '.', ENTER,

    };

    public HebrewKeyboard(Context context, OnClickListener onClickListener, Runnable backspace, int imeOptions) {
        super(context, onClickListener, backspace, imeOptions);
    }

    @Override
    protected int layout() {
        return R.layout.he_keyboard_layout;
    }

    @Override
    int nextLanguage() {
        return EnglishKeyboard.LANGUAGE_ID;
    }

    @Override
    protected char[] codes() {
        return hebrewKeyboardCodes;
    }

}
