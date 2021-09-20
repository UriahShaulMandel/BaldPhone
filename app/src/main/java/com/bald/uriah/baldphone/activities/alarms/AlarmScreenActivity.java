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

package com.bald.uriah.baldphone.activities.alarms;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.TimedBaldActivity;
import com.bald.uriah.baldphone.databases.alarms.Alarm;
import com.bald.uriah.baldphone.databases.alarms.AlarmScheduler;
import com.bald.uriah.baldphone.databases.alarms.AlarmsDatabase;
import com.bald.uriah.baldphone.utils.AnimationsUtils;
import com.bald.uriah.baldphone.core.BPrefs;
import com.bald.uriah.baldphone.core.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.DateTimeUtils;
import com.bald.uriah.baldphone.utils.S;

/**
 * Alarm screen, will be called from {@link com.bald.uriah.baldphone.broadcast_receivers.AlarmReceiver}
 */
public class AlarmScreenActivity extends TimedBaldActivity {
    private static final String TAG = AlarmScreenActivity.class.getSimpleName();
    private static final int TIME_DELAYED_SCHEDULE = 100;
    private static final AudioAttributes alarmAttributes =
            new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

    private TextView tv_name, snooze;
    private ImageView cancel;
    private Ringtone ringtone;
    private Alarm alarm;

    public static Ringtone getRingtone(Context context) {
        Uri alert =
                RingtoneManager
                        .getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_ALARM);
        if (alert == null)
            alert = RingtoneManager
                    .getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
        if (alert == null)
            alert = RingtoneManager
                    .getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        final Ringtone ringtone = RingtoneManager.getRingtone(context, alert);
        final AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {//who knows lol - btw don't delete user's may lower the alarm sounds by mistake
            final int alarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * (BPrefs.get(context).getInt(BPrefs.ALARM_VOLUME_KEY, BPrefs.ALARM_VOLUME_DEFAULT_VALUE) + 6) / 10;
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolume, 0);
        }
        ringtone.setAudioAttributes(alarmAttributes);
        return ringtone;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        S.logImportant("alarmScreen was called!");
        setContentView(R.layout.alarm_screen);

        attachXml();

        final Intent intent = getIntent();
        if (intent == null) throw new AssertionError();
        int key = intent.getIntExtra(Alarm.ALARM_KEY_VIA_INTENTS, -1);
        if (key == -1) throw new AssertionError();
        alarm = AlarmsDatabase.getInstance(this).alarmsDatabaseDao().getByKey(key);
        if (alarm == null) {
            S.logImportant("alarm == null!, returning");
            return;
        }

        final String name = alarm.getName();
        if (name == null) tv_name.setVisibility(View.GONE);
        else tv_name.setText(name);

        cancel.setOnClickListener(v -> {
            if (vibrator != null)
                vibrator.vibrate(D.vibetime);
            if (alarm.getName().equals(getString(R.string.timer)))
                AlarmsDatabase.getInstance(this).alarmsDatabaseDao().delete(alarm);
            finish();
        });
        cancel.setOnLongClickListener(v -> {
            if (vibrator != null)
                vibrator.vibrate(D.vibetime);
            if (alarm.getName().equals(getString(R.string.timer)))
                AlarmsDatabase.getInstance(this).alarmsDatabaseDao().delete(alarm);
            finish();
            return true;
        });

        snooze.setOnClickListener((v) -> snooze());
        snooze.setOnLongClickListener((v) -> {
            snooze();
            return true;
        });

        ringtone = getRingtone(this);
        try {
            ringtone.play();
        } catch (Exception e) {
            BaldToast.error(this);
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        AnimationsUtils.makeBiggerAndSmaller(this, cancel, () -> {
            if (vibrator != null) vibrator.vibrate(D.vibetime);
        });
        scheduleNextAlarm();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ringtone != null)
            ringtone.play();
    }

    @Override
    protected void onStop() {
        if (ringtone != null)
            ringtone.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (ringtone != null)
            ringtone.stop();
        super.onDestroy();
    }

    private void attachXml() {
        tv_name = findViewById(R.id.alarm_name);
        cancel = findViewById(R.id.alarm_cancel);
        snooze = findViewById(R.id.snooze);
    }

    private void snooze() {
        if (vibrator != null)
            vibrator.vibrate(D.vibetime);
        AlarmScheduler.scheduleSnooze(alarm, this);
        finish();
    }

    private void scheduleNextAlarm() {
        AlarmScheduler.cancelAlarm(alarm.getKey(), this);
        new Handler().postDelayed(() -> {
            if (alarm.isEnabled()) {
                if (alarm.getDays() == -1) {
                    AlarmsDatabase.getInstance(this)
                            .alarmsDatabaseDao().update(alarm.getKey(), false);
                } else {
                    AlarmScheduler.scheduleAlarm(alarm, this);
                }
            }
        }, TIME_DELAYED_SCHEDULE);
    }

    @Override
    public void onBackPressed() {
        snooze();
        super.onBackPressed();
    }

    @Override
    protected int screenTimeout() {
        return DateTimeUtils.MINUTE * 5;
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_SYSTEM_ALERT_WINDOW;
    }
}