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

package com.bald.uriah.baldphone.activities;

import android.app.Activity;

import androidx.test.rule.ActivityTestRule;

import com.bald.uriah.baldphone.utils.BPrefs;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import java.util.Locale;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public abstract class BaseActivityTest {
    private final LocaleRule mLocaleRule = new LocaleRule(Locale.ENGLISH, Locale.FRENCH, Locale.forLanguageTag("iw-IL"));
    private final ScreenshotWatcher mScreenshotWatcher = new ScreenshotWatcher();
    public ActivityTestRule<HomeScreenActivity> mActivityTestRule = new ActivityTestRule(activity(), true, false);
    @Rule
    public final RuleChain mRuleChain = RuleChain.outerRule(mLocaleRule)
            .around(mScreenshotWatcher)
            .around(mActivityTestRule);

    protected abstract Class<? extends Activity> activity();

    @Before
    public void setUp() {
        BPrefs
                .get(getInstrumentation().getTargetContext())
                .edit()
                .putBoolean(BPrefs.TEST_KEY, true)
                .putBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, true)
                .putBoolean(BPrefs.LONG_PRESSES_KEY, true)
                .putBoolean(BPrefs.LONG_PRESSES_SHORTER_KEY, false)
                .putBoolean(BPrefs.TOUCH_NOT_HARD_KEY, false)
                .commit();
    }

    @After
    public void after() {
        BPrefs
                .get(getInstrumentation().getTargetContext())
                .edit()
                .clear()
                .commit();
    }

    public void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
