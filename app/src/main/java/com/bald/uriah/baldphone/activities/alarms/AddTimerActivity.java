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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.databases.alarms.Alarm;
import com.bald.uriah.baldphone.databases.alarms.AlarmScheduler;
import com.bald.uriah.baldphone.databases.alarms.AlarmsDatabase;
import com.bald.uriah.baldphone.utils.D;

import org.joda.time.DateTime;

/**
 * Activity for creating {@link Alarm}, as Timers.
 */
public class AddTimerActivity extends BaldActivity {
    private static final String TAG = AddTimerActivity.class.getSimpleName();
    static final String ALARM_KEY_AS_EXTRA_KEY = "alarm";
    private int timeIn5Minutes = 1;
    private View bt_alarm_submit, bt_add, bt_dec;
    private TextView time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_alarm_quick);
        attachXml();
        genOnClickListeners();
        setupYoutube(6);
    }

    private void attachXml() {
        bt_alarm_submit = findViewById(R.id.bt_alarm_submit);
        bt_add = findViewById(R.id.bt_add);
        bt_dec = findViewById(R.id.bt_dec);
        time = findViewById(R.id.tv_number);
    }

    private void submit() {
        final String name = getString(R.string.timer);
        final DateTime now = DateTime.now();

        final Alarm alarm = new Alarm();
        alarm.setDays(-1);
        alarm.setHour(now.getHourOfDay());
        alarm.setMinute(now.getMinuteOfHour());
        alarm.addMinutes(5 * timeIn5Minutes);
        alarm.setEnabled(true);
        alarm.setName(name);

        final int key = (int) AlarmsDatabase.getInstance(this)
                .alarmsDatabaseDao().insert(alarm);
        alarm.setKey(key);
        AlarmScheduler.scheduleAlarm(alarm, this);

        setResult(
                Activity.RESULT_OK,
                new Intent()
                        .putExtra(Alarm.ALARM_KEY_VIA_INTENTS, alarm.getKey())
                        .putExtra(AddTimerActivity.ALARM_KEY_AS_EXTRA_KEY, alarm.getKey())
        );
        finish();

    }

    private void genOnClickListeners() {
        bt_alarm_submit.setOnClickListener((v) -> submit());
        bt_add.setOnClickListener(v -> {
            timeIn5Minutes++;
            updateAccordingToTime();
        });
        bt_dec.setOnClickListener(v -> {
            timeIn5Minutes--;
            updateAccordingToTime();
        });
        updateAccordingToTime();
    }

    @SuppressLint("DefaultLocale")
    private void updateAccordingToTime() {
        if (timeIn5Minutes < 1 || timeIn5Minutes > (D.DAY / (5 * D.MINUTE)))//its 288
            timeIn5Minutes = 1;
        time.setText(String.format("%d %s", timeIn5Minutes * 5, getString(R.string.minutes)));
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_SYSTEM_ALERT_WINDOW;
    }
}
