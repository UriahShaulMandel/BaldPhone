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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.activities.contacts.ShareActivity;
import com.bald.uriah.baldphone.content_providers.BaldFileProvider;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * S - Static. Static methods which are used everywhere in the platform.
 */
public class S {
    private static final String TAG = S.class.getSimpleName();
    public static final String BALD_IMPORTANT_MESSAGE = "Bald Important Message";
    private static final float DIM_AMOUNT = 0.5f;

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
                .addFlag(BDialog.FLAG_YES | BDialog.FLAG_NO)
                .setPositiveButtonListener(params -> {
                    S.share(baldActivity, new Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, baldActivity.getString(R.string.share_actual_text)));
                    return true;
                })
                .show();
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
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
        final Intent intent = packageManager.getLaunchIntentForPackage(packageName);
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
                .addFlag(BDialog.FLAG_YES | BDialog.FLAG_CANCEL)
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

    public static void applyDim(@NonNull ViewGroup parent) {
        final Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * DIM_AMOUNT));
        parent.getOverlay().add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        parent.getOverlay().clear();
    }

    public static <T> List<WeakReference<T>> cleanWeakList(List<WeakReference<T>> list) {
        final List<WeakReference<T>> ret = new ArrayList<>();
        for (WeakReference<T> tWeakReference : list) {
            final T t = tWeakReference.get();
            if (t != null)
                ret.add(new WeakReference<>(t));
        }
        return ret;
    }

    public static void showDropDownPopup(BaldActivity baldActivity, int windowsWidth, DropDownRecyclerViewAdapter.DropDownListener dropDownListener, View view) {
        final RelativeLayout dropDownContainer = (RelativeLayout) LayoutInflater.from(baldActivity).inflate(R.layout.drop_down_recycler_view, null, false);
        final RecyclerView recyclerView = dropDownContainer.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(baldActivity) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        final PopupWindow popupWindow = new PopupWindow(dropDownContainer, (int) (windowsWidth / 1.3),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 82 * dropDownListener.size(), baldActivity.getResources().getDisplayMetrics()), true);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(baldActivity)
                        .drawable(R.drawable.settings_divider)
                        .build()
        );

        recyclerView.setAdapter(new DropDownRecyclerViewAdapter(baldActivity, popupWindow, dropDownListener));

        final ViewGroup root = (ViewGroup) baldActivity.getWindow().getDecorView().getRootView();
        if (root == null) throw new AssertionError();
        popupWindow.setOnDismissListener(() -> {
            S.clearDim(root);
            dropDownListener.onDismiss();
        });
        popupWindow.setBackgroundDrawable(baldActivity.getDrawable(R.drawable.empty));
        popupWindow.showAsDropDown(view);
        baldActivity.autoDismiss(popupWindow);
        S.applyDim(root);
    }

    /**
     * https://github.com/bumptech/glide/issues/1484#issuecomment-365625087
     */
    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || "goldfish".equals(Build.HARDWARE)
                || "ranchu".equals(Build.HARDWARE)
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static void sendVersionInfo(final Context context) {
        if (!S.isEmulator() && !BuildConfig.DEBUG) {
            final SharedPreferences sharedPreferences = BPrefs.get(context);
            if (!sharedPreferences.contains(BPrefs.UUID_KEY)) {
                sharedPreferences.edit().putString(BPrefs.UUID_KEY, UUID.randomUUID().toString()).apply();
            }

            Volley.newRequestQueue(context).add(
                    new StringRequest(
                            Request.Method.GET,
                            String.format(Locale.US, "http://baldphone.co.nf/insert_new_install.php?uuid=%s&vcode=%d", sharedPreferences.getString(BPrefs.UUID_KEY, null), BuildConfig.VERSION_CODE),
                            response -> {
                            },
                            error -> {
                            }
                    ).setTag("baldphone_server"));
        }
    }
}