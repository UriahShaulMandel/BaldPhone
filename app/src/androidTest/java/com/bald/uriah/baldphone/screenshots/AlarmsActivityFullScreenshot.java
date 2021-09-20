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

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.alarms.AlarmsActivity;
import com.bald.uriah.baldphone.databases.alarms.Alarm;
import com.bald.uriah.baldphone.databases.alarms.AlarmsDatabase;
import com.bald.uriah.baldphone.utils.DateTimeUtils;

import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AlarmsActivityFullScreenshot extends BaseScreenshotTakerTest<AlarmsActivity> {

    public void test() {
        final Alarm alarm = new Alarm();
        alarm.setDays(DateTimeUtils.Days.ALL);
        alarm.setHour(8);
        alarm.setMinute(30);
        alarm.setEnabled(true);
        alarm.setName(getInstrumentation().getTargetContext().getString(R.string.morning));
        AlarmsDatabase.getInstance(getInstrumentation().getTargetContext()).alarmsDatabaseDao().insert(alarm);
        mActivityTestRule.launchActivity(new Intent());
        AlarmsDatabase.getInstance(getInstrumentation().getTargetContext()).alarmsDatabaseDao().deleteAll();
    }

    @Override
    protected Class<AlarmsActivity> activity() {
        return AlarmsActivity.class;
    }
}
