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
import com.bald.uriah.baldphone.activities.contacts.ContactsActivity;
import com.tomash.androidcontacts.contactgetter.main.ContactDataFactory;
import com.tomash.androidcontacts.contactgetter.main.contactsSaver.ContactsSaverBuilder;

import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ContactsActivityScreenshot extends BaseScreenshotTakerTest<ContactsActivity> {

    public void test() {
        mActivityTestRule.launchActivity(new Intent());
        getInstrumentation().waitForIdleSync();
        new Handler(mActivityTestRule.getActivity().getMainLooper())
                .post(() -> {
                    final ContactsActivity dis = mActivityTestRule.getActivity();
                    final String[] names = dis.getResources().getStringArray(R.array.names_for_screenshots);
                    for (final String name : names) {
                        new ContactsSaverBuilder(dis).saveContact(ContactDataFactory.createEmpty().setCompositeName(name));
                    }
                    dis.applyFilter();

                });
        getInstrumentation().waitForIdleSync();

    }

    @Override
    protected void cleanupAfterTest() {
        super.cleanupAfterTest();
        TestUtils.deleteAllContactsInEmulator(getInstrumentation().getTargetContext().getApplicationContext());
    }

    @Override
    protected Class<ContactsActivity> activity() {
        return ContactsActivity.class;
    }

}
