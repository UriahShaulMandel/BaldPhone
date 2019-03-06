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

package com.bald.uriah.baldphone.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.crashlytics.android.Crashlytics;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bald.uriah.baldphone.activities.PermissionActivity.EXTRA_INTENT;


/**
 * the parent of all of the activitys in this app.
 */
public abstract class BaldActivity extends AppCompatActivity implements SensorEventListener {
    protected static final int
            PERMISSION_NONE = 0,
            PERMISSION_WRITE_SETTINGS = 0b1,
            PERMISSION_DEFAULT_PHONE_HANDLER = 0b10,
            PERMISSION_READ_CONTACTS = 0b100 | PERMISSION_DEFAULT_PHONE_HANDLER,
            PERMISSION_WRITE_CONTACTS = 0b1000 | PERMISSION_DEFAULT_PHONE_HANDLER,
            PERMISSION_CALL_PHONE = 0b10000 | PERMISSION_DEFAULT_PHONE_HANDLER,
            PERMISSION_READ_CALL_LOG = 0b100000 | PERMISSION_DEFAULT_PHONE_HANDLER,
            PERMISSION_CAMERA = 0b1000000,
            PERMISSION_WRITE_EXTERNAL_STORAGE = 0b10000000,
            PERMISSION_NOTIFICATION_LISTENER = 0b100000000 | PERMISSION_WRITE_SETTINGS,
            PERMISSION_REQUEST_INSTALL_PACKAGES = 0b1000000000;
    private static final String TAG = BaldActivity.class.getSimpleName();
    protected Vibrator vibrator;
    @StyleRes
    private int themeIndex;
    private List<WeakReference<Dialog>> dialogsToClose = new ArrayList<>(1);
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean near;
    private int touches;
    private int accidentalMinTouches = 3, accidentalTime = 3 * D.SECOND;
    private boolean useAccidentalGuard = true;
    private Handler handler;
    private Runnable touchesDecreaser = () -> touches = (touches -= 1) < 0 ? 0 : touches;

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
        if ((requiredPermissions & PERMISSION_READ_CALL_LOG) != 0) {
            if (ActivityCompat.checkSelfPermission(activity, READ_CALL_LOG) != PERMISSION_GRANTED)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.getPackageManager().canRequestPackageInstalls()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * not removing yet, perhaps the issue with google will be solved
     */
    static boolean defaultDialerGranted(BaldActivity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            final TelecomManager telecomManager = (TelecomManager) activity.getSystemService(TELECOM_SERVICE);
//            return telecomManager != null && Objects.equals(activity.getPackageName(), telecomManager.getDefaultDialerPackage());
//        }
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
        Fabric.with(this, new Crashlytics());
        if (!checkPermissions(this, requiredPermissions())) {
            startActivity(new Intent(this, PermissionActivity.class)
                    .putExtra(PermissionActivity.EXTRA_REQUIRED_PERMISSIONS, requiredPermissions())
                    .putExtra(PermissionActivity.EXTRA_NAME, activityName())
                    .putExtra(EXTRA_INTENT, getIntent())
            );
            finish();
            return;
        }

        handler = new Handler();
        final SharedPreferences sharedPreferences = BPrefs.get(this);
        vibrator = sharedPreferences
                .getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE)
                ? (Vibrator) getSystemService(VIBRATOR_SERVICE) : null;
        themeIndex = S.getTheme(this);
        setTheme(themeIndex);

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
            if (dialog != null) {
                dialog.dismiss();
            }
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

    public void autoDismissDialog(Dialog dialog) {
        dialogsToClose.add(new WeakReference<>(dialog));
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
                    .setDialogState(BDialog.DialogState.OK)
                    .setCancelable(false)
                    .setPositiveButtonListener(params -> {
                        touches = 0;
                        accidentalMinTouches += 2;
                        accidentalTime /= 2;
                        return true;
                    })
                    .show();

        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        near = event.values[0] == 0;
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

    //TODO mabye remove this
    protected String activityName() {
        return "";
    }
}
