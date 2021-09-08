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

import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.bald.uriah.baldphone.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BaldToast {
    public static final int LENGTH_SEC = -1;
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_INFORMATIVE = 2;
    @LayoutRes
    private final static int layout = R.layout.toast_layout;
    @DrawableRes
    private static final int TYPE_DEFAULT_BACKGROUND_COLOR_RES_ID =
            R.drawable.toast_default_background;
    @ColorRes
    private static final int TYPE_DEFAULT_FOREGROUND_COLOR_RES_ID =
            R.color.toast_foreground_default;
    @DrawableRes
    private static final int TYPE_ERROR_BACKGROUND_COLOR_RES_ID =
            R.drawable.toast_error_background;
    @ColorRes
    private static final int TYPE_ERROR_FOREGROUND_COLOR_RES_ID =
            R.color.toast_foreground_error;
    @DrawableRes
    private static final int TYPE_INFORMATIVE_BACKGROUND_COLOR_RES_ID =
            R.drawable.toast_informative_background;
    @ColorRes
    private static final int TYPE_INFORMATIVE_FOREGROUND_COLOR_RES_ID =
            R.color.toast_foreground_informative;
    private final Context context;
    @ToastType
    private int type = TYPE_DEFAULT;
    private CharSequence text;
    private boolean big = false;
    private int duration = Toast.LENGTH_LONG;
    private Toast toast;
    private boolean built;

    private BaldToast(@NonNull Context context) {
        this.context = new ContextThemeWrapper(context.getApplicationContext(), R.style.bald_light);
    }

    public static BaldToast from(@NonNull Context context) {
        return new BaldToast(context);
    }

    public static void error(Context context) {
        BaldToast.from(context).setText(R.string.an_error_has_occurred).setType(TYPE_ERROR).show();
    }

    public static void simple(Context context, CharSequence text) {
        BaldToast.from(context).setText(text).setType(TYPE_DEFAULT).show();
    }

    public static void simple(Context context, @StringRes int resId) {
        BaldToast.from(context).setText(context.getText(resId)).setType(TYPE_DEFAULT).show();
    }

    public static void longer(Context context) {
        BaldToast.from(context).setText(R.string.press_longer).setType(TYPE_DEFAULT).setLength(-1).show();
    }

    public BaldToast setType(@ToastType int type) {
        this.type = type;
        return this;
    }

    public BaldToast setText(CharSequence text) {
        this.text = text;
        return this;
    }

    public BaldToast setText(@StringRes int resString) {
        this.text = context.getString(resString);
        return this;
    }

    public BaldToast setLength(@IntRange(from = -1, to = 1) int duration) {
        this.duration = duration;
        return this;
    }

    public BaldToast setBig(boolean big) {
        this.big = big;
        return this;
    }

    public void show() {
        if (!built)
            build();
        toast.show();
        if (duration == LENGTH_SEC) {
            new Handler()
                    .postDelayed(() -> toast.cancel(), D.SECOND);
        }
    }

    public BaldToast build() {
        //not sure why but removing this line crashes app! so don't
        final View toastView = LayoutInflater.from(context).inflate(layout, null);
        final TextView textView = (TextView) toastView;
        if (big)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize() * 2);

        final @DrawableRes int toastViewBackground;
        final @ColorInt int textViewColor;
        switch (type) {
            case TYPE_DEFAULT:
                toastViewBackground = TYPE_DEFAULT_BACKGROUND_COLOR_RES_ID;
                textViewColor = context.getResources().getColor(TYPE_DEFAULT_FOREGROUND_COLOR_RES_ID);
                break;
            case TYPE_ERROR:
                toastViewBackground = TYPE_ERROR_BACKGROUND_COLOR_RES_ID;
                textViewColor = context.getResources().getColor(TYPE_ERROR_FOREGROUND_COLOR_RES_ID);
                break;
            case TYPE_INFORMATIVE:
                toastViewBackground = TYPE_INFORMATIVE_BACKGROUND_COLOR_RES_ID;
                textViewColor = context.getResources().getColor(TYPE_INFORMATIVE_FOREGROUND_COLOR_RES_ID);
                break;
            default:
                throw new IllegalArgumentException("type not supported!");
        }

        textView.setTextColor(textViewColor);
        toastView.setBackground(ContextCompat.getDrawable(context, toastViewBackground));

        textView.setText(text);
        toast = new Toast(context);
        toast.setDuration(duration == LENGTH_SEC ? Toast.LENGTH_SHORT : duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(toastView);
        built = true;
        return this;
    }

    @IntDef({TYPE_DEFAULT, TYPE_ERROR, TYPE_INFORMATIVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ToastType {
    }
}
