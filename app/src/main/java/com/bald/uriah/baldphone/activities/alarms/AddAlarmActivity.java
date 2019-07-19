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

package com.bald.uriah.baldphone.activities.alarms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import androidx.annotation.Nullable;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.databases.alarms.Alarm;
import com.bald.uriah.baldphone.databases.alarms.AlarmScheduler;
import com.bald.uriah.baldphone.databases.alarms.AlarmsDatabase;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.views.BaldButton;
import com.bald.uriah.baldphone.views.BaldNumberChooser;
import org.joda.time.DateTime;

/**
 * Activity for creating\editing {@link Alarm}.
 * if called with extra {@link #ALARM_KEY_AS_EXTRA_KEY} it'll edit the alarm
 */
public class AddAlarmActivity extends com.bald.uriah.baldphone.activities.BaldActivity {
    static final String ALARM_KEY_AS_EXTRA_KEY = "alarm";
    private static final String TAG = AddAlarmActivity.class.getSimpleName();
    private int alarmKeyToEdit = -1;
    private Vibrator vibrator;
    private BaldButton bt_alarm_submit;
    private EditText alarm_edit_name;
    private BaldNumberChooser chooser_hours, chooser_minutes;
    private CheckBox[] daysCheckBoxes;
    private RadioButton only_once, every_day;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_alarm_activity);
        vibrator = BPrefs.get(this)
                .getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE) ?
                (Vibrator) getSystemService(VIBRATOR_SERVICE) : null;
        attachXml();
        genOnClickListeners();

        if (getIntent().hasExtra(ALARM_KEY_AS_EXTRA_KEY)) {
            final int alarmIndex = getIntent().getIntExtra(ALARM_KEY_AS_EXTRA_KEY, -1);
            if (alarmIndex == -1)
                throw new IllegalArgumentException("ALARM_KEY_AS_EXTRA_KEY cannot be -1 or not defind!");
            final Alarm alarm = AlarmsDatabase.getInstance(this).alarmsDatabaseDao().getByKey(alarmIndex);
            alarmKeyToEdit = alarm.getKey();
            chooser_hours.setNumber(alarm.getHour());
            chooser_minutes.setNumber(alarm.getMinute());
            if (alarm.getName() != null)
                alarm_edit_name.setText(alarm.getName());
            final int alarmDays = alarm.getDays();
            if (alarmDays == -1)
                only_once.setChecked(true);
            else
                for (int i = 0; i < daysCheckBoxes.length; i++)
                    daysCheckBoxes[i].setChecked((alarmDays | (D.Days.SUNDAY << i)) == alarmDays);
        } else {
            DateTime now = DateTime.now();
            chooser_hours.setNumber(now.getHourOfDay());
            chooser_minutes.setNumber(now.getMinuteOfHour());
        }

        setupYoutube(7);
    }

    private void attachXml() {
        bt_alarm_submit = findViewById(R.id.bt_alarm_submit);
        alarm_edit_name = findViewById(R.id.alarm_edit_name);
        chooser_hours = findViewById(R.id.chooser_hours);
        chooser_minutes = findViewById(R.id.chooser_minutes);
        daysCheckBoxes = new CheckBox[]{
                findViewById(R.id.sunday),
                findViewById(R.id.monday),
                findViewById(R.id.tuesday),
                findViewById(R.id.wednesday),
                findViewById(R.id.thursday),
                findViewById(R.id.friday),
                findViewById(R.id.saturday)
        };
        only_once = findViewById(R.id.rb_once);
        every_day = findViewById(R.id.rb_every_day);
        only_once.setChecked(true);

    }

    private void submit() {
        String name = alarm_edit_name.getText().toString();
        if (TextUtils.isEmpty(name)) name = this.getString(R.string.alarm);

        int sum = 0;
        if (every_day.isChecked()) {
            sum = D.Days.ALL;
        } else {
            for (int i = 0; i < daysCheckBoxes.length; i++)
                sum |= daysCheckBoxes[i].isChecked() ? D.Days.SUNDAY << i : 0;

            if (sum == 0) sum = -1;
        }

        final Alarm alarm = new Alarm();
        alarm.setDays(sum);
        alarm.setHour(chooser_hours.getNumber());
        alarm.setMinute(chooser_minutes.getNumber());
        alarm.setEnabled(true);
        alarm.setName(name);

        if (alarmKeyToEdit == -1) {
            final int key = (int) AlarmsDatabase.getInstance(this)
                    .alarmsDatabaseDao().insert(alarm);
            alarm.setKey(key);
        } else {
            AlarmScheduler.cancelAlarm(alarmKeyToEdit, this);
            alarm.setKey(alarmKeyToEdit);
            AlarmsDatabase.getInstance(this).alarmsDatabaseDao().replace(alarm);
        }
        AlarmScheduler.scheduleAlarm(alarm, this);
        setResult(Activity.RESULT_OK, new Intent()
                .putExtra(Alarm.ALARM_KEY_VIA_INTENTS, alarm.getKey())
                .putExtra(AddAlarmActivity.ALARM_KEY_AS_EXTRA_KEY, alarm.getKey()));
        finish();
    }

    private void genOnClickListeners() {
        bt_alarm_submit.setOnClickListener((v) -> submit());
        only_once.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (vibrator != null) vibrator.vibrate(D.vibetime);
            if (isChecked) {
                for (CheckBox checkBox : daysCheckBoxes)
                    checkBox.setChecked(false);
                every_day.setChecked(false);
            }
        });
        only_once.setOnLongClickListener(v -> {
            only_once.setChecked(true);
            return true;
        });

        every_day.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (vibrator != null) vibrator.vibrate(D.vibetime);
                if (isChecked) {
                    for (CheckBox checkBox : daysCheckBoxes)
                        checkBox.setChecked(false);
                    only_once.setChecked(false);
                    every_day.setOnCheckedChangeListener(null);
                    every_day.setChecked(true);
                    every_day.setOnCheckedChangeListener(this);
                }
            }
        });
        every_day.setOnLongClickListener(v -> {
            every_day.setChecked(true);
            return true;
        });

        for (final CheckBox checkBox : daysCheckBoxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (vibrator != null)
                    vibrator.vibrate(D.vibetime);
                if (isChecked) {
                    only_once.setChecked(false);
                    every_day.setChecked(false);
                }
                boolean anyIsChecked = false;
                boolean allAreChecked = true;
                for (CheckBox cb : daysCheckBoxes) {
                    if (cb.isChecked()) {
                        anyIsChecked = true;
                    } else {
                        allAreChecked = false;
                    }
                }
                if (!anyIsChecked) {
                    only_once.setChecked(true);
                } else if (allAreChecked) {
                    for (CheckBox cb : daysCheckBoxes)
                        cb.setChecked(false);
                    every_day.setChecked(true);
                }
            });
            checkBox.setOnLongClickListener(v -> {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
                return true;
            });
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
