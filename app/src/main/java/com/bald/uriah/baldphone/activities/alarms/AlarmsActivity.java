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

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.databases.alarms.Alarm;
import com.bald.uriah.baldphone.databases.alarms.AlarmScheduler;
import com.bald.uriah.baldphone.databases.alarms.AlarmsDatabase;
import com.bald.uriah.baldphone.utils.BaldGridItemDecoration;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldSwitch;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

import org.acra.ACRA;

import java.util.Collections;
import java.util.List;

/**
 * Activity for viewing the enabled {@link Alarm}
 * Each alarm can be edited and deleted from this activity
 */
public class AlarmsActivity extends com.bald.uriah.baldphone.activities.BaldActivity {
    private static final String TAG = AlarmsActivity.class.getSimpleName();
    public static final int ADD_ALARM_ACTIVITY_REQUEST_CODE = 1;
    private LinearLayout bt_cancel_all_alarms, bt_add_alarm, bt_quickly_add_alarm;

    private RecyclerView recyclerView;
    private AlarmsRecyclerViewAdapter adapter;
    private List<Alarm> alarmList = Collections.EMPTY_LIST;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);
        attachXml();
        adapter = new AlarmsRecyclerViewAdapter(this);

        final WindowManager windowManager = getWindowManager();
        final Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        final boolean screenOrientation = (point.x / point.y) != 0;
        int numberOfAppsInARow = screenOrientation ? 2 : 1;
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfAppsInARow);
        recyclerView.setLayoutManager(gridLayoutManager);
        Resources r = getResources();
        recyclerView.addItemDecoration(new BaldGridItemDecoration(r.getDimensionPixelSize(R.dimen.divider), numberOfAppsInARow, getDrawable(R.drawable.ll_divider), r.getDimensionPixelSize(R.dimen.padding_dividers)));

        recyclerView.setAdapter(adapter);

        genOnClickListeners();

        setupYoutube(5);

    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AlarmsDatabase.getInstance(this).alarmsDatabaseDao().getNumberOfRows() != alarmList.size())
            refreshViews();
    }

    public void refreshViews() {
        alarmList = AlarmsDatabase.getInstance(this).alarmsDatabaseDao().getAllSortedByTime();
        adapter.notifyDataSetChanged();
    }

    private void attachXml() {
        recyclerView = findViewById(R.id.child);
        bt_add_alarm = findViewById(R.id.bt_add_alarm);
        bt_quickly_add_alarm = findViewById(R.id.bt_quickly_add_alarm);
        bt_cancel_all_alarms = findViewById(R.id.bt_cancel_all_alarms);
    }

    private void genOnClickListeners() {
        bt_cancel_all_alarms.setOnClickListener(v -> S.showAreYouSureYouWantToDelete(getString(R.string.all_alarms), this, this::cancelAllAlarms));
        bt_add_alarm.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(AlarmsActivity.this, AddAlarmActivity.class), ADD_ALARM_ACTIVITY_REQUEST_CODE)
        );
        bt_quickly_add_alarm.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(AlarmsActivity.this, AddTimerActivity.class), ADD_ALARM_ACTIVITY_REQUEST_CODE)
        );
    }

    private void cancelAllAlarms() {
        final AlarmsDatabase alarmsDatabase = AlarmsDatabase.getInstance(this);
        final List<Alarm> alarmList = alarmsDatabase
                .alarmsDatabaseDao().getAllEnabled();
        for (Alarm alarm : alarmList)
            AlarmScheduler.cancelAlarm(alarm.getKey(), this);

        alarmsDatabase.alarmsDatabaseDao().deleteAll();

        refreshViews();
        BaldToast.from(this).setText(R.string.removed_all_alarms).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ALARM_ACTIVITY_REQUEST_CODE) {
            if (resultCode != RESULT_CANCELED) {
                int key = data.getIntExtra(Alarm.ALARM_KEY_VIA_INTENTS, -1);
                if (key == -1) {
                    Log.wtf(TAG, "Key should never be -1");
                    ACRA.getErrorReporter().handleSilentException(new AssertionError("key cannot be -1"));
                    BaldToast.error(this);
                    finish();
                    return;
                }

                final Alarm newAlarm = AlarmsDatabase.getInstance(this).alarmsDatabaseDao().getByKey(key);
                final String name = newAlarm.getName();
                final String message = String.format(getString(R.string.new_alarm___was_created), name == null || name.equals("") ? "" : getString(R.string.named__) + " " + name);
                BaldToast.from(this).setText(message).setType(BaldToast.TYPE_INFORMATIVE).show();
                final long nextTimeAlarmWillWork = AlarmScheduler.nextTimeAlarmWillWorkInMsFromNow(newAlarm);
                final String message2;
                if (nextTimeAlarmWillWork < D.HOUR)
                    message2 = String.format(getString(R.string.to___minuets_from_now), nextTimeAlarmWillWork / D.MINUTE);
                else if (nextTimeAlarmWillWork < D.DAY)
                    message2 = String.format(getString(R.string.to___hours_and___minutes_from_now), nextTimeAlarmWillWork / D.HOUR, (nextTimeAlarmWillWork % D.HOUR) / D.MINUTE);
                else
                    message2 = String.format(getString(R.string.to___days_and___hours_from_now), nextTimeAlarmWillWork / D.DAY, (nextTimeAlarmWillWork % D.DAY) / D.HOUR);

                BaldToast.from(this).setText(message2).setType(BaldToast.TYPE_INFORMATIVE).show();
            }
        }
    }

    class AlarmsRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<AlarmsRecyclerViewAdapter.ViewHolder> {
        private final LayoutInflater inflater;

        AlarmsRecyclerViewAdapter(AlarmsActivity alarmsActivity) {
            this.inflater = LayoutInflater.from(alarmsActivity);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.large_alarm_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.update(alarmList.get(position));
        }

        @Override
        public int getItemCount() {
            return alarmList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final BaldSwitch alarm_switch;
            final View bt_delete, bt_edit;
            final TextView alarm_time, alarm_name, repeating_days;

            public ViewHolder(View itemView) {
                super(itemView);
                bt_delete = itemView.findViewById(R.id.bt_delete);
                bt_edit = itemView.findViewById(R.id.bt_edit);
                alarm_switch = itemView.findViewById(R.id.alarm_switch);
                alarm_time = itemView.findViewById(R.id.alarm_time);
                alarm_name = itemView.findViewById(R.id.alarm_name);
                repeating_days = itemView.findViewById(R.id.repeating_days);
            }

            public void update(Alarm alarm) {
                final CharSequence message;
                if (alarm.getDays() == -1)
                    message = AlarmsActivity.this.getString(R.string.will_repeat_only_once);
                else if (alarm.getDays() == (D.Days.ALL))
                    message = AlarmsActivity.this.getString(R.string.repeats_every_day);
                else {

                    StringBuilder stringBuilder = new StringBuilder(40);
                    stringBuilder.append(AlarmsActivity.this.getText(R.string.alarms_repeats_every));
                    stringBuilder.append(' ');
                    for (int day : D.Days.ARRAY_ALL)
                        if ((alarm.getDays() & day) == day) {
                            stringBuilder.append(AlarmsActivity.this.getString(S.balddayToStringId(day)));
                            stringBuilder.append(", ");
                        }
                    stringBuilder.setLength(stringBuilder.length() - 2);
                    stringBuilder.append('.');
                    message = stringBuilder;
                }

                repeating_days.setText(message);

                alarm_time.setText(S.numberToAlarmString(alarm.getHour(), alarm.getMinute()));
                alarm_name.setText(alarm.getName());
                alarm_switch.setChecked(alarm.isEnabled());
                alarm_switch.setOnChangeListener(isChecked -> {
                    AlarmsDatabase.getInstance(AlarmsActivity.this)
                            .alarmsDatabaseDao().update(alarm.getKey(), isChecked);
                    alarm.setEnabled(isChecked);

                    if (isChecked) {
                        AlarmScheduler.scheduleAlarm(alarm, AlarmsActivity.this);
                    } else {
                        AlarmScheduler.cancelAlarm(alarm.getKey(), AlarmsActivity.this);
                    }
                });

                bt_edit.setOnClickListener((v) ->
                        AlarmsActivity.this.startActivity(
                                new Intent(AlarmsActivity.this, AddAlarmActivity.class)
                                        .putExtra(AddAlarmActivity.ALARM_KEY_AS_EXTRA_KEY, alarm.getKey())
                        )
                );
                bt_delete.setOnClickListener((v) ->
                        S.showAreYouSureYouWantToDelete(alarm.getName(), AlarmsActivity.this, () -> {
                            AlarmsDatabase.getInstance(AlarmsActivity.this)
                                    .alarmsDatabaseDao().deleteByIds(alarm.getKey());
                            AlarmsActivity.this.refreshViews();
                        }));
            }
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
