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

package com.bald.uriah.baldphone.activities.pills;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.databases.reminders.Reminder;
import com.bald.uriah.baldphone.databases.reminders.ReminderScheduler;
import com.bald.uriah.baldphone.databases.reminders.RemindersDatabase;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.views.BaldButton;
import com.bald.uriah.baldphone.views.BaldMultipleSelection;
import com.bald.uriah.baldphone.views.BaldTitleBar;

import static com.bald.uriah.baldphone.utils.BaldToast.TYPE_ERROR;

public class AddPillActivity extends BaldActivity {
    private static final String TAG = AddPillActivity.class.getSimpleName();
    public static final int INDEX_CUSTOM = 5;
    static final String REMINDER_KEY_AS_EXTRA_KEY = "reminder";
    public static int[] COLORS = new int[]{
            R.color.blue,
            R.color.red,
            R.color.gray,
            R.color.yellow,
            R.color.green,
    };
    private int reminderIdToEdit = -1;
    private Vibrator vibrator;
    private BaldButton bt_submit;
    private BaldTitleBar baldTitleBar;
    private EditText reminder_edit_name;
    private BaldMultipleSelection baldMultipleSelection;
    private ImageView[] colors;
    private int selectedColor = 0;
    private byte[] customColor;
    private CheckBox[] daysCheckBoxes;
    private RadioButton every_day;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_pill_activity);
        vibrator = getSharedPreferences(BPrefs.KEY, MODE_PRIVATE).getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE) ? (Vibrator) getSystemService(VIBRATOR_SERVICE) : null;
        attachXml();
        genOnClickListeners();

        if (getIntent().hasExtra(REMINDER_KEY_AS_EXTRA_KEY)) {
            final int remindersIndex = getIntent().getIntExtra(REMINDER_KEY_AS_EXTRA_KEY, -1);
            if (remindersIndex == -1)
                throw new IllegalArgumentException("REMINDER_KEY_AS_EXTRA_KEY cannot be -1!!");
            final Reminder reminder = RemindersDatabase.getInstance(this).remindersDatabaseDao().getById(remindersIndex);
            reminderIdToEdit = reminder.getId();

            baldMultipleSelection.setSelection(reminder.getStartingTime());

            if (reminder.getBinaryContentType() == Reminder.BINARY_RGB) {
                customColor = reminder.getBinaryContent();
                colors[INDEX_CUSTOM].setVisibility(View.VISIBLE);
                colors[INDEX_CUSTOM].setImageTintList(ColorStateList.valueOf(Color.rgb(customColor[0] & 0xFF, customColor[1] & 0xFF, customColor[2] & 0xFF)));

                colors[selectedColor].setBackgroundResource(R.drawable.style_for_buttons_transparent);
                selectedColor = INDEX_CUSTOM;
                colors[selectedColor].setBackgroundResource(R.drawable.btn_selected);
            }

            if (reminder.getTextualContent() != null)
                reminder_edit_name.setText(reminder.getTextualContent());

            final int alarmDays = reminder.getDays();
            if (alarmDays == D.Days.ALL)
                every_day.setChecked(true);
            else {
                for (int i = 0; i < daysCheckBoxes.length; i++) {
                    daysCheckBoxes[i].setChecked((alarmDays | (D.Days.SUNDAY << i)) == alarmDays);
                }
            }

        }

    }

    private void attachXml() {
        bt_submit = findViewById(R.id.bt_submit);
        baldTitleBar = findViewById(R.id.bald_title_bar);
        reminder_edit_name = findViewById(R.id.reminder_edit_name);
        baldMultipleSelection = findViewById(R.id.multiple_selection);
        baldMultipleSelection.addSelection(R.string.morning, R.string.afternoon, R.string.evening);
        daysCheckBoxes = new CheckBox[]{
                findViewById(R.id.sunday),
                findViewById(R.id.monday),
                findViewById(R.id.tuesday),
                findViewById(R.id.wednesday),
                findViewById(R.id.thursday),
                findViewById(R.id.friday),
                findViewById(R.id.saturday)
        };
        every_day = findViewById(R.id.rb_once);
        colors = new ImageView[]{
                findViewById(R.id.blue),
                findViewById(R.id.red),
                findViewById(R.id.gray),
                findViewById(R.id.yellow),
                findViewById(R.id.green),
                findViewById(R.id.custom),
        };

        every_day.setChecked(true);

    }

    private void submit2() {

        final String name = reminder_edit_name.getText().toString();
        int sum = 0;

        if (!every_day.isChecked()) {
            for (int i = 0; i < daysCheckBoxes.length; i++)
                sum |= daysCheckBoxes[i].isChecked() ? D.Days.SUNDAY << i : 0;
        } else
            sum = D.Days.ALL;

        if (sum == 0) {
            BaldToast.from(this).setType(TYPE_ERROR).setText(R.string.at_least_one_day_must_be_selected).show();
            return;
        }

        final Reminder reminder = new Reminder();
        reminder.setStartingTime(baldMultipleSelection.getSelection());
        reminder.setDays(sum);
        reminder.setTextualContent(name);
        reminder.setBinaryContentType(Reminder.BINARY_RGB);
        reminder.setReminderType(Reminder.TYPE_PILL);

        if (selectedColor != INDEX_CUSTOM) {
            int color = ActivityCompat.getColor(this, COLORS[selectedColor]);
            reminder.setBinaryContent(new byte[]{(byte) Color.red(color), (byte) Color.green(color), (byte) Color.blue(color)});
        } else {
            reminder.setBinaryContent(customColor);
        }

        if (reminderIdToEdit == -1) {
            int id = (int) RemindersDatabase.getInstance(this).remindersDatabaseDao().insert(reminder);
            reminder.setId(id);
        } else {
            reminder.setId(reminderIdToEdit);
            RemindersDatabase.getInstance(this).remindersDatabaseDao().replace(reminder);
            ReminderScheduler.scheduleReminder(reminder, this);
        }
        ReminderScheduler.scheduleReminder(reminder, this);

        setResult(RESULT_OK, new Intent().putExtra(Reminder.REMINDER_KEY_VIA_INTENTS, reminder.getId())
                .putExtra(AddPillActivity.REMINDER_KEY_AS_EXTRA_KEY, reminder.getId()));
        finish();

    }

    private void genOnClickListeners() {
        bt_submit.setOnClickListener((v) -> submit2());

        every_day.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (vibrator != null) vibrator.vibrate(D.vibetime);
            if (isChecked) {
                for (CheckBox checkBox : daysCheckBoxes) {
                    checkBox.setChecked(false);
                }
            }
        });
        every_day.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                every_day.setChecked(true);
                return true;
            }
        });

        for (final CheckBox checkBox : daysCheckBoxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (vibrator != null) vibrator.vibrate(D.vibetime);
                if (isChecked)
                    every_day.setChecked(false);
                else {
                    boolean anyIsNotChecked = false;
                    for (CheckBox cb : daysCheckBoxes) {
                        if (!cb.isChecked()) {
                            anyIsNotChecked = true;
                            break;
                        }
                    }
                    if (!anyIsNotChecked) {
                        every_day.setChecked(true);
                        for (final CheckBox checkBox2 : daysCheckBoxes)
                            checkBox2.setChecked(false);
                    }
                }
            });
            checkBox.setOnLongClickListener(v -> {
                if (vibrator != null) vibrator.vibrate(D.vibetime);
                checkBox.setChecked(!checkBox.isChecked());
                return true;
            });
        }

        for (int i = 0; i < colors.length; i++) {
            int finalI = i;
            colors[i].setOnClickListener(v -> {
                if (selectedColor != finalI) {
                    colors[selectedColor].setBackgroundResource(R.drawable.style_for_buttons_transparent);
                    selectedColor = finalI;
                    colors[selectedColor].setBackgroundResource(R.drawable.btn_selected);
                }
            });
        }

    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
