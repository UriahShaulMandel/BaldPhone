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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.contacts.ContactsActivity;
import com.tomash.androidcontacts.contactgetter.main.ContactDataFactory;
import com.tomash.androidcontacts.contactgetter.main.contactsSaver.ContactsSaverBuilder;

import org.junit.runner.RunWith;

import java.util.Locale;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ContactsActivityScreenshot extends BaseScreenshotTakerTest<ContactsActivity> {

    public void test() {
        final Context context = getInstrumentation().getTargetContext().getApplicationContext();

        Resources resources = context.getResources();
        Locale.setDefault(locales[localeIndex]);
        Configuration config = resources.getConfiguration();
        config.setLocale(locales[localeIndex]);
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        resources.updateConfiguration(config, displayMetrics);

        final String[] names = resources.getStringArray(R.array.names_for_screenshots);
        for (final String name : names) {
            new ContactsSaverBuilder(context).saveContact(ContactDataFactory.createEmpty().setCompositeName(name));
        }

        mActivityTestRule.launchActivity(new Intent());

    }

    @Override
    protected Class<ContactsActivity> activity() {
        return ContactsActivity.class;
    }
}