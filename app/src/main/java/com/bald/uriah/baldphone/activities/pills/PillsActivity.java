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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.databases.reminders.Reminder;
import com.bald.uriah.baldphone.databases.reminders.ReminderScheduler;
import com.bald.uriah.baldphone.databases.reminders.RemindersDatabase;
import com.bald.uriah.baldphone.core.BaldToast;
import com.bald.uriah.baldphone.utils.DateTimeUtils;
import com.bald.uriah.baldphone.utils.RecyclerViewUtils;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PillsActivity extends BaldActivity {
    private static final int ADD_REMINDER_REQUEST_CODE = 6699;
    private List<Reminder> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private View bt_add, bt_time_changer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        attachXml();
        genOnClickListeners();

        final WindowManager windowManager = getWindowManager();
        final Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        final boolean screenOrientation = (point.x / point.y) != 0;
        int numberOfAppsInARow = screenOrientation ? 2 : 1;
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfAppsInARow);
        recyclerView.setLayoutManager(gridLayoutManager);
        Resources r = getResources();
        recyclerView.addItemDecoration(
                new RecyclerViewUtils.BaldGridItemDecoration(r.getDimensionPixelSize(R.dimen.divider),
                        numberOfAppsInARow,
                        getDrawable(R.drawable.ll_divider),
                        r.getDimensionPixelSize(R.dimen.padding_dividers)));

        recyclerView.setAdapter(new PillsRecyclerViewAdapter());

    }

    @Override
    protected void onStart() {
        super.onStart();
        list = RemindersDatabase.getInstance(this).remindersDatabaseDao().getAllRemindersOrderedByTime();
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void attachXml() {
        recyclerView = findViewById(R.id.recycler_view);
        bt_add = findViewById(R.id.bt_add);
        bt_time_changer = findViewById(R.id.bt_time_changer);
    }

    private void genOnClickListeners() {
        bt_add.setOnClickListener(v -> startActivityForResult(new Intent(this, AddPillActivity.class), ADD_REMINDER_REQUEST_CODE));
        bt_time_changer.setOnClickListener(v -> startActivity(new Intent(this, PillTimeSetterActivity.class)));
    }

    private void cancelAllAlarms() {
        final RemindersDatabase remindersDatabase = RemindersDatabase.getInstance(this);
        final List<Reminder> reminderList =
                remindersDatabase.remindersDatabaseDao()
                        .getAllRemindersOrderedByTime();
        for (Reminder reminder : reminderList)
            ReminderScheduler.cancelReminder(reminder.getId(), this);
        remindersDatabase.remindersDatabaseDao().deleteAll();

        refreshViews();
        BaldToast.from(this).setText(R.string.removed_all_alarms).show();
    }

    public void refreshViews() {
        list = RemindersDatabase.getInstance(this).remindersDatabaseDao().getAllRemindersOrderedByTime();
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_REMINDER_REQUEST_CODE)
            if (resultCode == RESULT_OK) {
                BaldToast.from(this).setText(R.string.pill_added).show();
                refreshViews();
            }

        super.onActivityResult(requestCode, resultCode, data);
    }

    class PillsRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<PillsRecyclerViewAdapter.ViewHolder> {
        private final LayoutInflater inflater;

        public PillsRecyclerViewAdapter() {
            this.inflater = LayoutInflater.from(PillsActivity.this);
        }

        @NonNull
        @Override
        public PillsActivity.PillsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PillsActivity.PillsRecyclerViewAdapter.ViewHolder(inflater.inflate(R.layout.large_reminder_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PillsActivity.PillsRecyclerViewAdapter.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.update(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View bt_delete, bt_edit;
            final TextView reminder_time, reminder_textual_content, repeating_days;
            final ImageView iv_pill;

            public ViewHolder(View itemView) {
                super(itemView);
                bt_delete = itemView.findViewById(R.id.bt_delete);
                bt_edit = itemView.findViewById(R.id.bt_edit);
                reminder_time = itemView.findViewById(R.id.reminder_time);
                reminder_textual_content = itemView.findViewById(R.id.reminder_textual_content);
                repeating_days = itemView.findViewById(R.id.repeating_days);
                iv_pill = itemView.findViewById(R.id.iv_pill);
            }

            public void update(Reminder reminder) {
                final CharSequence message;

                if (reminder.getDays() == (DateTimeUtils.Days.ALL))
                    message = PillsActivity.this.getString(R.string.repeats_every_day);
                else {

                    StringBuilder stringBuilder = new StringBuilder(20);
//                    stringBuilder.append(PillsActivity.this.getText(R.string.reminder_repeats_every));
//                    stringBuilder.append(' ');
                    for (int day : DateTimeUtils.Days.ARRAY_ALL)
                        if ((reminder.getDays() & day) == day) {
                            stringBuilder.append(PillsActivity.this.getString(DateTimeUtils.balddayToStringId(day)));
                            stringBuilder.append(", ");
                        }
                    stringBuilder.setLength(stringBuilder.length() - 2);
                    stringBuilder.append('.');
                    message = stringBuilder;
                }

                repeating_days.setText(message);

                reminder_time.setText(reminder.getTimeAsStringRes());
                reminder_textual_content.setText(reminder.getTextualContent());

                bt_edit.setOnClickListener((v) ->
                        PillsActivity.this.startActivityForResult(
                                new Intent(PillsActivity.this, AddPillActivity.class)
                                        .putExtra(AddPillActivity.REMINDER_KEY_AS_EXTRA_KEY, reminder.getId()),
                                ADD_REMINDER_REQUEST_CODE
                        )
                );
                bt_delete.setOnClickListener((v) ->
                        S.showAreYouSureYouWantToDelete(reminder.getTextualContent(), PillsActivity.this, () -> {
                            RemindersDatabase.getInstance(PillsActivity.this).remindersDatabaseDao().removeReminders(reminder.getId());
                            list.remove(reminder);
                            notifyDataSetChanged();
                        }));

                if (reminder.getBinaryContentType() == Reminder.BINARY_RGB) {
                    final Drawable drawable = getDrawable(R.drawable.pill).mutate();
                    drawable.setTint(Color.rgb(
                            reminder.getBinaryContent()[0] & 0xFF,
                            reminder.getBinaryContent()[1] & 0xFF,
                            reminder.getBinaryContent()[2] & 0xFF));
                    iv_pill.setImageDrawable(drawable);

                }
            }
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_SYSTEM_ALERT_WINDOW;
    }
}
