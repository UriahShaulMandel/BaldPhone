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

import android.app.Activity;
import android.graphics.Bitmap;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.screenshot.Screenshot;

import com.bald.uriah.baldphone.utils.BPrefs;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.IOException;
import java.util.Locale;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public abstract class BaseScreenshotTakerTest<T extends Activity> {
    private static final Locale[] locales = new Locale[]{Locale.ENGLISH, Locale.FRENCH, Locale.forLanguageTag("iw-IL")};
    private static int localeIndex = 0;
    private final LocaleRule mLocaleRule = new LocaleRule(locales);
    public ActivityTestRule<T> mActivityTestRule = new ActivityTestRule<T>(activity(), true, false);
    @Rule
    public final RuleChain mRuleChain = RuleChain.outerRule(mLocaleRule)
            .around(mActivityTestRule);

    protected abstract Class<T> activity();

    @Test
    public void actualTest() throws InterruptedException {
        test();
        getInstrumentation().waitForIdleSync();
        Thread.sleep(800);
        try {
            Screenshot.capture().setFormat(Bitmap.CompressFormat.PNG).setName(locales[localeIndex++].getLanguage()).process();
            localeIndex = localeIndex % locales.length;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    protected abstract void test();

    @Before
    public void setUp() {
        BPrefs
                .get(getInstrumentation().getTargetContext())
                .edit()
                .putBoolean(BPrefs.TEST_KEY, true)
                .putInt(BPrefs.THEME_KEY, BPrefs.Themes.LIGHT)
                .putBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, true)
                .putBoolean(BPrefs.LONG_PRESSES_KEY, false)
                .putBoolean(BPrefs.LONG_PRESSES_SHORTER_KEY, false)
                .putBoolean(BPrefs.TOUCH_NOT_HARD_KEY, true)
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
