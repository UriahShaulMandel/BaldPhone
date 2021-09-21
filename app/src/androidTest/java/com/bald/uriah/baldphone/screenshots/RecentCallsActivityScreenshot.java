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

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.apps.recent_calls.RecentActivity;

import org.joda.time.DateTime;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.bald.uriah.baldphone.screenshots.FakeCallsRecyclerViewAdapter.FakeCall;
import static com.bald.uriah.baldphone.screenshots.FakeCallsRecyclerViewAdapter.INCOMING_TYPE;
import static com.bald.uriah.baldphone.screenshots.FakeCallsRecyclerViewAdapter.MISSED_TYPE;
import static com.bald.uriah.baldphone.screenshots.FakeCallsRecyclerViewAdapter.OUTGOING_TYPE;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecentCallsActivityScreenshot extends BaseScreenshotTakerTest<RecentActivity> {

    public void test() {
        mActivityTestRule.launchActivity(new Intent());
        getInstrumentation().waitForIdleSync();
        new Handler(mActivityTestRule.getActivity().getMainLooper())
                .post(() -> {
                    final RecentActivity dis = mActivityTestRule.getActivity();
                    final String[] names = dis.getResources().getStringArray(R.array.names_for_screenshots);
                    final String[] numbers = dis.getResources().getStringArray(R.array.phone_numbers_for_tests);
                    final List<FakeCall> fakeCallList = new ArrayList<>();
                    int[] types = new int[]
                            {INCOMING_TYPE, INCOMING_TYPE, INCOMING_TYPE, OUTGOING_TYPE, OUTGOING_TYPE, OUTGOING_TYPE, INCOMING_TYPE, MISSED_TYPE};
                    int[] actualNames = new int[]
                            {4, 0, 2, 1, 0, 1, 3, 0};
                    int[] minusDays = new int[]
                            {0, 4, 2, 0, 0, 1, 2, 0};
                    int[] minusHours = new int[]
                            {1, 2, 0, 0, 0, 1, 2, 0};
                    int[] minusMinutes = new int[]
                            {23, 12, 7, 34, 2, 2, 35, 7};
                    for (int i = 0; i < types.length; i++)
                        fakeCallList.add(
                                new FakeCall(
                                        types[i],
                                        null,
                                        names[actualNames[i]],
                                        numbers[0],
                                        DateTime.now()
                                                .minusDays(minusDays[i])
                                                .minusHours(minusHours[i])
                                                .minusMinutes(minusMinutes[i])
                                                .getMillis()
                                )
                        );
                    Collections.sort(fakeCallList, (o1, o2) -> Long.compare(o2.dateTime, o1.dateTime));
                    dis.recyclerView.setAdapter(new FakeCallsRecyclerViewAdapter(fakeCallList, dis));
                });
        getInstrumentation().waitForIdleSync();

    }

    @Override
    protected void cleanupAfterTest() {
        super.cleanupAfterTest();
    }

    @Override
    protected Class<RecentActivity> activity() {
        return RecentActivity.class;
    }
}
