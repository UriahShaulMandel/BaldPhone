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
import android.widget.TextView;

import com.bald.uriah.baldphone.R;

@SuppressLint("ViewConstructor")
public class EnglishKeyboard extends BaldKeyboard implements BaldKeyboard.Capitalised {
    public static final int LANGUAGE_ID = 2;
    public static final char[] usKeyboardCodes = new char[]{
            'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'
            , 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ',',
            SHIFT, 'z', 'x', 'c', 'v', 'b', 'n', 'm', BACKSPACE,
            NUMBERS, LANGUAGE, SPEECH_TO_TEXT, ' ', HIDE, '.', ENTER,
    };
    public static final char[] usKeyboardCodesCAPS = new char[]{
            'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'
            , 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', ',',
            SHIFT, 'Z', 'X', 'C', 'V', 'B', 'N', 'M', BACKSPACE,
            NUMBERS, LANGUAGE, SPEECH_TO_TEXT, ' ', HIDE, '.', ENTER,
    };

    private boolean caps;

    public EnglishKeyboard(Context context, OnClickListener onClickListener, Runnable backspace, int imeOptions) {
        super(context, onClickListener, backspace, imeOptions);
    }

    public void setCaps() {
        caps = !caps;
        final char[] codes = codes();
        for (int i = 0; i < children.length; i++) {
            children[i].setTag(codes[i]);
            if (children[i] instanceof TextView)
                ((TextView) children[i]).setText(new char[]{codes[i]}, 0, 1);
        }
    }

    @Override
    protected int layout() {
        return R.layout.us_keyboard_layout;
    }

    @Override
    int nextLanguage() {
        return HebrewKeyboard.LANGUAGE_ID;
    }

    @Override
    protected char[] codes() {
        return caps ? usKeyboardCodesCAPS : usKeyboardCodes;
    }
}