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
import android.os.Vibrator;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Keep;
import androidx.annotation.LayoutRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bald.uriah.baldphone.utils.S;

public abstract class BaldKeyboard extends FrameLayout {
    public static final char SHIFT = (char) 1;
    public static final char LANGUAGE = (char) 2;
    public static final char NUMBERS = (char) 3;
    public static final char ENTER = (char) 4;
    public static final char BACKSPACE = (char) 5;
    public static final char SPEECH_TO_TEXT = (char) 6;
    public static final char HIDE = (char) 7;

    protected final ConstraintLayout keyboard;
    protected final View[] children;
    protected final View backspace;
    private final Vibrator vibrator;
    private final Runnable backspaceRunnable;
    private Thread backspaceThread;

    @Keep
    public BaldKeyboard(Context context, View.OnClickListener onClickListener, Runnable backspaceRunnable) {
        super(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.backspaceRunnable = backspaceRunnable;
        final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, S.getTheme(context));
        keyboard = (ConstraintLayout) LayoutInflater.from(contextThemeWrapper).inflate(layout(), this, false);
        children = new View[keyboard.getChildCount()];
        for (int i = 0; i < children.length - 1/*cause of space view...*/; i++) {
            final View view = keyboard.getChildAt(i + 1/*cause of space view...*/);
            view.setOnClickListener(onClickListener);
            view.setTag((char) i);
            children[i] = view;
        }
        backspace = keyboard.getChildAt(backspaceIndex());
        backspace.setOnTouchListener(getBackSpaceListener());
        addView(keyboard);
    }

    public static BaldKeyboard newInstance(int language, Context context, View.OnClickListener onClickListener, Runnable backspaceRunnable) {
        switch (language) {
            case HebrewKeyboard.LANGUAGE_ID:
                return new HebrewKeyboard(context, onClickListener, backspaceRunnable);
            case EnglishKeyboard.LANGUAGE_ID:
                return new EnglishKeyboard(context, onClickListener, backspaceRunnable);
            case NumberKeyboard.LANGUAGE_ID:
                return new NumberKeyboard(context, onClickListener, backspaceRunnable);
            default:
                throw new IllegalArgumentException("language must be 0/1/2, it is currently:" + language);
        }
    }

    @LayoutRes
    protected abstract int layout();

    abstract char[] codes();

    protected abstract int backspaceIndex();

    abstract int nextLanguage();

    private View.OnTouchListener getBackSpaceListener() {
        final Runnable backspaceRunnable = new Runnable() {
            int counter = 0;//do not delete!

            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    counter = 0;
                    while (true) {
                        Thread.sleep(100);
                        counter++;
                        vibrator.vibrate(60);
                        BaldKeyboard.this.backspaceRunnable.run();
                        if (counter > 100)
                            return;
                    }
                } catch (Exception ignore) {
                }
            }
        };

        return new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    backspaceThread = new Thread(backspaceRunnable);
                    backspaceThread.start();
                }
                if (event.getAction() == MotionEvent.ACTION_UP)
                    if (BaldKeyboard.this.backspaceRunnable != null)
                        backspaceThread.interrupt();
                return false;
            }
        };
    }

    @FunctionalInterface
    interface Capitalised {
        void setCaps();
    }
}
