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

package com.bald.uriah.baldphone.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldTitleBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_CALL_LOG;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bald.uriah.baldphone.activities.PermissionActivity.EXTRA_INTENT;

/**
 * the parent of all of the activitys in this app.
 */
public abstract class BaldActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = BaldActivity.class.getSimpleName();
    protected static final int
            PERMISSION_NONE = 0,
            PERMISSION_READ_PHONE_STATE = 0b100000000000,
            PERMISSION_WRITE_SETTINGS = 0b1,
            PERMISSION_DEFAULT_PHONE_HANDLER = 0b10,
            PERMISSION_READ_CONTACTS = 0b100 | PERMISSION_DEFAULT_PHONE_HANDLER,
            PERMISSION_WRITE_CONTACTS = 0b1000 | PERMISSION_DEFAULT_PHONE_HANDLER,
            PERMISSION_CALL_PHONE = 0b10000 | PERMISSION_DEFAULT_PHONE_HANDLER | PERMISSION_READ_PHONE_STATE,
            PERMISSION_READ_CALL_LOG = 0b10000000000 | PERMISSION_DEFAULT_PHONE_HANDLER,
            PERMISSION_WRITE_CALL_LOG = 0b100000 | PERMISSION_DEFAULT_PHONE_HANDLER | PERMISSION_READ_CALL_LOG,
            PERMISSION_CAMERA = 0b1000000,
            PERMISSION_WRITE_EXTERNAL_STORAGE = 0b10000000,
            PERMISSION_NOTIFICATION_LISTENER = 0b100000000 | PERMISSION_WRITE_SETTINGS,
            PERMISSION_REQUEST_INSTALL_PACKAGES = 0b1000000000;

    public boolean testing = false;
    public boolean colorful;
    protected Vibrator vibrator;
    @StyleRes
    private int themeIndex;
    private List<WeakReference<Dialog>> dialogsToClose = new ArrayList<>(1);
    private List<WeakReference<PopupWindow>> popupWindowsToClose = new ArrayList<>(1);
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean near;
    private int touches;
    private int accidentalMinTouches = 3, accidentalTime = 3 * D.SECOND;
    private boolean useAccidentalGuard = true;
    private Handler handler;
    private Runnable touchesDecreaser = () -> touches = (touches -= 1) < 0 ? 0 : touches;

    @Override
    public void
    applyOverrideConfiguration(Configuration cfgOverride) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // add this to fix androidx.appcompat:appcompat 1.1.0 bug
            // which happens on Android 6.x ~ 7.x
            getResources();
        }

        super.applyOverrideConfiguration(cfgOverride);
    }

    /**
     * @return true if all permissions are granted.
     */
    public static boolean checkPermissions(BaldActivity activity, final int requiredPermissions) {
        if (requiredPermissions == PERMISSION_NONE)
            return true;
        if ((requiredPermissions & PERMISSION_DEFAULT_PHONE_HANDLER) != 0) {
            if (!defaultDialerGranted(activity))
                return false;
        }

        if ((requiredPermissions & PERMISSION_WRITE_SETTINGS) != 0) {
            if (!writeSettingsGranted(activity))
                return false;
            else if ((requiredPermissions & PERMISSION_NOTIFICATION_LISTENER) == PERMISSION_NOTIFICATION_LISTENER) {
                if (!notificationListenerGranted(activity))
                    return false;
            }
        }
        if ((requiredPermissions & PERMISSION_READ_CONTACTS) != 0) {
            if (ActivityCompat.checkSelfPermission(activity, READ_CONTACTS) != PERMISSION_GRANTED)
                return false;
        }
        if ((requiredPermissions & PERMISSION_WRITE_CONTACTS) != 0) {
            if (ActivityCompat.checkSelfPermission(activity, WRITE_CONTACTS) != PERMISSION_GRANTED)
                return false;
        }
        if ((requiredPermissions & PERMISSION_CALL_PHONE) != 0) {
            if (ActivityCompat.checkSelfPermission(activity, CALL_PHONE) != PERMISSION_GRANTED)
                return false;
        }
        if ((requiredPermissions & PERMISSION_WRITE_CALL_LOG) != 0) {
            if (!BuildConfig.FLAVOR.equals("gPlay"))
                if (ActivityCompat.checkSelfPermission(activity, WRITE_CALL_LOG) != PERMISSION_GRANTED)
                    return false;
        }
        if ((requiredPermissions & PERMISSION_READ_CALL_LOG) != 0) {
            if (!BuildConfig.FLAVOR.equals("gPlay"))
                if (ActivityCompat.checkSelfPermission(activity, READ_CALL_LOG) != PERMISSION_GRANTED)
                    return false;
        }
        if ((requiredPermissions & PERMISSION_READ_PHONE_STATE) != 0) {
            if (BPrefs.get(activity).getBoolean(BPrefs.DUAL_SIM_KEY, BPrefs.DUAL_SIM_DEFAULT_VALUE))
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
                    if (ActivityCompat.checkSelfPermission(activity, READ_PHONE_STATE) != PERMISSION_GRANTED)
                        return false;
        }
        if ((requiredPermissions & PERMISSION_CAMERA) != 0) {
            if (ActivityCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED)
                return false;
        }
        if ((requiredPermissions & PERMISSION_WRITE_EXTERNAL_STORAGE) != 0) {
            if (ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
                return false;
        }
        if ((requiredPermissions & PERMISSION_REQUEST_INSTALL_PACKAGES) != 0) {
            return
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                            (!BuildConfig.FLAVOR.equals("baldUpdates")) ||
                            activity.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * not removing yet, perhaps the issue with google will be solved
     */
    static boolean defaultDialerGranted(BaldActivity activity) {
        return true;
    }

    static boolean writeSettingsGranted(BaldActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(activity);
        }
        return true;
    }

    static boolean notificationListenerGranted(BaldActivity activity) {
        final String listeners = Settings.Secure.getString(activity.getContentResolver(), "enabled_notification_listeners");
        return listeners != null && listeners.contains(activity.getApplicationContext().getPackageName());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = BPrefs.get(this);
        testing = sharedPreferences.getBoolean(BPrefs.TEST_KEY, BPrefs.TEST_DEFAULT_VALUE);

        if (!checkPermissions(this, requiredPermissions())) {
            startActivity(new Intent(this, PermissionActivity.class)
                    .putExtra(PermissionActivity.EXTRA_REQUIRED_PERMISSIONS, requiredPermissions())
                    .putExtra(EXTRA_INTENT, getIntent())
            );
            finish();
            return;
        }

        handler = new Handler();
        vibrator = sharedPreferences
                .getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE)
                ? (Vibrator) getSystemService(VIBRATOR_SERVICE) : null;
        colorful = sharedPreferences.getBoolean(BPrefs.COLORFUL_KEY, BPrefs.COLORFUL_DEFAULT_VALUE);
        themeIndex = S.getTheme(this);
        setTheme(themeIndex);

        final int statusBar = sharedPreferences.getInt(BPrefs.STATUS_BAR_KEY, BPrefs.STATUS_BAR_DEFAULT_VALUE);
        if (statusBar != 2) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (statusBar != 0) {
            final Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(D.DEFAULT_STATUS_BAR_COLOR);
        }

        if (useAccidentalGuard = sharedPreferences.getBoolean(BPrefs.USE_ACCIDENTAL_GUARD_KEY, BPrefs.USE_ACCIDENTAL_GUARD_DEFAULT_VALUE)) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sensorManager != null) {
                proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            } else {
                useAccidentalGuard = false;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (themeIndex != S.getTheme(this))
            recreate();
        if (useAccidentalGuard)
            sensorManager.registerListener(this, proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        for (WeakReference<Dialog> dialogWeakReference : dialogsToClose) {
            final Dialog dialog = dialogWeakReference.get();
            if (dialog != null)
                dialog.dismiss();
        }

        for (WeakReference<PopupWindow> windowWeakReference : popupWindowsToClose) {
            final PopupWindow window = windowWeakReference.get();
            if (window != null)
                window.dismiss();
        }

        if (useAccidentalGuard)
            sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (vibrator != null)
            vibrator.vibrate(D.vibetime);
        super.onBackPressed();
    }

    public void autoDismiss(Dialog dialog) {
        if (dialogsToClose.size() > 10)
            dialogsToClose = S.cleanWeakList(dialogsToClose);
        dialogsToClose.add(new WeakReference<>(dialog));
    }

    public void autoDismiss(PopupWindow popupWindow) {
        if (popupWindowsToClose.size() > 10)
            popupWindowsToClose = S.cleanWeakList(popupWindowsToClose);
        popupWindowsToClose.add(new WeakReference<>(popupWindow));
    }

    @Override
    public void onUserInteraction() {
        if (useAccidentalGuard) {
            if (near) {
                Log.e(TAG, "onUserInteraction: ");
                if (touches >= 0) {
                    touches += 1;
                    accidentalTouchChecker();
                    handler.postDelayed(touchesDecreaser, accidentalTime);
                }
            } else touches = 0;
        }
    }

    private void accidentalTouchChecker() {
        if (touches > accidentalMinTouches) {
            touches = -1;
            handler.removeCallbacks(touchesDecreaser);
            BDB.from(this)
                    .setTitle(R.string.accidental_touches)
                    .setSubText(R.string.accidental_touches_subtext)
                    .addFlag(BDialog.FLAG_NOT_CANCELABLE | BDialog.FLAG_OK)
                    .setPositiveButtonListener(params -> {
                        touches = 0;
                        accidentalMinTouches += 2;
                        accidentalTime /= 2;
                        return true;
                    })
                    .show();

        }
    }

    public void onSensorChanged(SensorEvent event) {
        near = event.values[0] == 0;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * be sure {@link #setContentView(int)} was called, and the rootview contains a {@link BaldTitleBar} whose id is R.id.bald_title_bar.
     *
     * @param index the youtube video index. see
     */
    protected void setupYoutube(int index) {
        final View baldTitleBar = findViewById(R.id.bald_title_bar);
        if (baldTitleBar instanceof BaldTitleBar) {
            ((BaldTitleBar) baldTitleBar).getBt_help()
                    .setOnClickListener(
                            (v) -> startActivity(
                                    new Intent(this, YoutubeActivity.class)
                                            .putExtra(YoutubeActivity.EXTRA_ID, index)
                            )
                    );
        }

    }

    protected abstract int requiredPermissions();
}
