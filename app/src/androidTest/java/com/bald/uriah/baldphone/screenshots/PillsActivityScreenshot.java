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

package com.bald.uriah.baldphone.screenshots;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.pills.AddPillActivity;
import com.bald.uriah.baldphone.activities.pills.PillsActivity;
import com.bald.uriah.baldphone.databases.reminders.Reminder;
import com.bald.uriah.baldphone.databases.reminders.RemindersDatabase;
import com.bald.uriah.baldphone.utils.DateTimeUtils;

import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PillsActivityScreenshot extends BaseScreenshotTakerTest<PillsActivity> {

    public void test() {
        mActivityTestRule.launchActivity(new Intent());
        getInstrumentation().waitForIdleSync();
        new Handler(mActivityTestRule.getActivity().getMainLooper())
                .post(() -> {
                    final PillsActivity dis = mActivityTestRule.getActivity();

                    final Reminder medication_1 = new Reminder();
                    medication_1.setStartingTime(Reminder.TIME_MORNING);
                    medication_1.setDays(DateTimeUtils.Days.ALL ^ DateTimeUtils.Days.SUNDAY);
                    medication_1.setTextualContent(dis.getString(R.string.medication_1));
                    medication_1.setBinaryContentType(Reminder.BINARY_RGB);
                    medication_1.setReminderType(Reminder.TYPE_PILL);
                    final int color_1 = ActivityCompat.getColor(dis, AddPillActivity.COLORS[0]);
                    medication_1.setBinaryContent(new byte[]{(byte) Color.red(color_1), (byte) Color.green(color_1), (byte) Color.blue(color_1)});

                    final Reminder medication_2 = new Reminder();
                    medication_2.setStartingTime(Reminder.TIME_MORNING);
                    medication_2.setDays(DateTimeUtils.Days.SUNDAY);
                    medication_2.setTextualContent(dis.getString(R.string.medication_2));
                    medication_2.setBinaryContentType(Reminder.BINARY_RGB);
                    medication_2.setReminderType(Reminder.TYPE_PILL);
                    final int color_2 = ActivityCompat.getColor(dis, AddPillActivity.COLORS[4]);
                    medication_2.setBinaryContent(new byte[]{(byte) Color.red(color_2), (byte) Color.green(color_2), (byte) Color.blue(color_2)});
                    RemindersDatabase.getInstance(dis).remindersDatabaseDao().insertAll(medication_1, medication_2);

                    dis.refreshViews();
                });
        getInstrumentation().waitForIdleSync();

    }

    @Override
    protected void cleanupAfterTest() {
        super.cleanupAfterTest();
        RemindersDatabase.getInstance(getInstrumentation().getTargetContext().getApplicationContext()).remindersDatabaseDao().deleteAll();
    }

    @Override
    protected Class<PillsActivity> activity() {
        return PillsActivity.class;
    }
}
