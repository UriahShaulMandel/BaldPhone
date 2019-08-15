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

package com.bald.uriah.baldphone.screenshots;

import android.content.Intent;
import android.os.Handler;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.bald.uriah.baldphone.activities.RecentActivity;

import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecentCallsActivityScreenshot extends BaseScreenshotTakerTest<RecentActivity> {

    public void test() {
        mActivityTestRule.launchActivity(new Intent());
        getInstrumentation().waitForIdleSync();
        new Handler(mActivityTestRule.getActivity().getMainLooper())
                .post(() -> {
//                    final RecentActivity dis = mActivityTestRule.getActivity();
//
//                    final String[] names = dis.getResources().getStringArray(R.array.names_for_screenshots);
//                    final ContactData[] contacts = new ContactData[names.length];
//                    for (int i = 0; i < names.length; i++) {
//                        contacts[i] = new ContactsGetterBuilder(dis).getById(new ContactsSaverBuilder(dis).saveContact(ContactDataFactory.createEmpty().setCompositeName(names[i])));
//                    }
//
//                    List<CallLog.Calls> callsList= new ArrayList<>();
//                    callsList.add(new Call(contacts[0].get));
//
//
//                    dis.recyclerView.setAdapter(
//                            new CallsRecyclerViewAdapter(Arrays.asList(
//                                    new Call()
//                            ), dis)
//                    );

                });
        getInstrumentation().waitForIdleSync();

    }

    @Override
    protected void cleanupAfterTest() {
        super.cleanupAfterTest();
        TestUtils.deleteAllContactsInEmulator(getInstrumentation().getTargetContext().getApplicationContext());
    }

    @Override
    protected Class<RecentActivity> activity() {
        return RecentActivity.class;
    }
}
