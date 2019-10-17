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
import android.os.Handler;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.bald.uriah.baldphone.activities.pills.PillsActivity;
import com.bald.uriah.baldphone.databases.reminders.RemindersDatabase;

import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PillsEmptyActivityScreenshot extends BaseScreenshotTakerTest<PillsActivity> {

    public void test() {
        mActivityTestRule.launchActivity(new Intent());
        getInstrumentation().waitForIdleSync();
        new Handler(mActivityTestRule.getActivity().getMainLooper())
                .post(() -> {
                    final PillsActivity dis = mActivityTestRule.getActivity();
                    RemindersDatabase.getInstance(dis).remindersDatabaseDao().deleteAll();
                    dis.refreshViews();
                });
        getInstrumentation().waitForIdleSync();

    }

    @Override
    protected Class<PillsActivity> activity() {
        return PillsActivity.class;
    }
}
