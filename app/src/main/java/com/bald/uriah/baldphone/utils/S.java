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

package com.bald.uriah.baldphone.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.activities.contacts.ShareActivity;
import com.bald.uriah.baldphone.content_providers.BaldFileProvider;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * S - Static. Static methods which are used everywhere in the platform.
 */
public class S {
    public static final String BALD_IMPORTANT_MESSAGE = "Bald Important Message";
    private static final String TAG = S.class.getSimpleName();

    public static void logImportant(@Nullable CharSequence charSequence) {
        Log.e(BALD_IMPORTANT_MESSAGE, String.valueOf(charSequence));
    }

    public static void share(@NonNull final Context context, @NonNull final Intent intent) {
        context.startActivity(new Intent(context, ShareActivity.class).putExtra(ShareActivity.EXTRA_SHARABLE_URI, intent));
    }

    public static void shareBaldPhone(@NonNull final BaldActivity baldActivity) {
        BDB.from(baldActivity)
                .setTitle(R.string.share_baldphone)
                .setSubText(R.string.share_bald_phone_subtext)
                .setDialogState(BDialog.DialogState.YES_NO)
                .setPositiveButtonListener(params -> {
                    S.share(baldActivity, new Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, baldActivity.getString(R.string.share_actual_text)));
                    return true;
                })
                .show();
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @StringRes
    public static int balddayToStringId(int baldDay) {
        switch (baldDay) {
            case 0:
                throw new RuntimeException("0 is not defined in a baldday int");
            case -1:
                throw new RuntimeException("-1 doesn't have a String id");
            case D.Days.SUNDAY:
                return R.string.sunday;
            case D.Days.MONDAY:
                return R.string.monday;
            case D.Days.TUESDAY:
                return R.string.tuesday;
            case D.Days.WEDNESDAY:
                return R.string.wednesday;
            case D.Days.THURSDAY:
                return R.string.thursday;
            case D.Days.FRIDAY:
                return R.string.friday;
            case D.Days.SATURDAY:
                return R.string.saturday;

        }
        throw new RuntimeException(baldDay + " is not defined in a specific baldday int");
    }

    public static boolean isPackageInstalled(@NonNull Context context, @NonNull String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    public static String numberToAlarmString(int hours, int minutes) {
        return String.format("%s:%s", hours < 10 ? "0" + hours : hours, minutes < 10 ? "0" + minutes : minutes);
    }

    // convert from bitmap to byte array
    public static byte[] bitmapToByteArray(@NonNull Bitmap bitmap) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap byteArrayToBitmap(@NonNull byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


    @StyleRes
    public static int getTheme(@NonNull Context context) {
        @StyleRes int theme = BPrefs.Themes.THEMES[context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE).getInt(BPrefs.THEME_KEY, BPrefs.THEME_DEFAULT_VALUE)];
        if (theme == -1) {
            int hour = DateTime.now().getHourOfDay();
            if (hour > 6 && hour < 19)
                return BPrefs.Themes.THEMES[BPrefs.Themes.LIGHT];
            return BPrefs.Themes.THEMES[BPrefs.Themes.DARK];
        }
        return theme;
    }

    @NonNull
    public static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static String stringTimeFromLong(@NonNull Context context, long timeStamp, boolean withHoursAndMinutes) {
        final DateTime now = DateTime.now(); //immutable
        //time will be before now. so this is checking if event occurred today
        final DateTime dateTime = new DateTime(timeStamp);
        Calendar c = Calendar.getInstance();
        c.setTime(dateTime.toDate());
        if (dateTime.isAfter(now.withMillisOfDay(0))) {
            return withHoursAndMinutes ? S.numberToAlarmString(dateTime.getHourOfDay(), dateTime.getMinuteOfHour())
                    :
                    context.getString(R.string.today)
                    ;
        } else if (dateTime.isAfter(now.withMillisOfDay(0).minusDays(1))) {
            return withHoursAndMinutes ?
                    String.format("%s %s", context.getString(R.string.yesterday), S.numberToAlarmString(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()))
                    :
                    context.getString(R.string.yesterday);
        } else if (dateTime.isAfter(now.withMillisOfDay(0).withDayOfWeek(1))) {
            return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        } else {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(timeStamp);
        }
    }


    public static int[] intArrFromString(@NonNull String string) {
        final String[] splitted = string
                .substring(1, string.length() - 1)
                .split(",");
        final int[] ret = new int[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
            ret[i] = Integer.parseInt(splitted[i]);
        }
        return ret;

    }

    public static int[] typedArrayToResArray(@NonNull Resources resources, @ArrayRes int resId) {
        final TypedArray ar = resources.obtainTypedArray(resId);
        final int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++)
            resIds[i] = ar.getResourceId(i, 0);
        ar.recycle();
        return resIds;
    }

    /**
     * @param o an object to find its string value
     * @return the object's {@link Object#toString()}; Empty string if the object is null
     */
    @NonNull
    public static String str(@Nullable Object o) {
        return o == null ? "" : o.toString();
    }

    //iterates via old fashioned for and not via foreach, because on most android devices its faster.
    public static boolean intArrayContains(@NonNull final int[] array, final int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value)
                return true;
        }
        return false;
    }

    public static void showAreYouSureYouWantToDelete(@NonNull final String what, @NonNull final BaldActivity baldActivity, @NonNull final Runnable deleteRunnable) {
        final CharSequence title = String.format(baldActivity.getString(R.string.delete___), what);
        final CharSequence message = String.format(baldActivity.getString(R.string.are_you_sure_you_want_to_delete___), what);
        BDB.from(baldActivity)
                .setTitle(title)
                .setSubText(message)
                .setPositiveButtonListener((params -> {
                    deleteRunnable.run();
                    return true;
                }))
                .setDialogState(BDialog.DialogState.YES_CANCEL)
                .setCancelable(true)
                .setBaldActivityToAutoDismiss(baldActivity)
                .show();
    }

    public static Uri fileToUriCompat(final File file, final Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                BaldFileProvider.getUriForFile(context, context.getString(R.string.authorities), file)
                :
                Uri.fromFile(file);
    }

    public static void startComponentName(final Context context, final ComponentName componentName) {
        context.startActivity(Intent.makeRestartActivityTask(componentName));
    }
}
