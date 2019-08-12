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
import android.graphics.Canvas;
import android.view.View;

import androidx.test.rule.ActivityTestRule;

import com.bald.uriah.baldphone.utils.BPrefs;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.File;
import java.io.FileOutputStream;
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
        File screenshotsFolder = new File("/sdcard/Pictures/screenshots");
        if (!screenshotsFolder.exists())
            screenshotsFolder.mkdir();
        try (FileOutputStream out = new FileOutputStream("/sdcard/Pictures/screenshots/" + getClass().getSimpleName() + "_" + locales[localeIndex++].getLanguage() + ".png")) {
            final Bitmap bitmap = screenShot(mActivityTestRule.getActivity().getWindow().getDecorView().getRootView());
            if (bitmap == null)
                throw new AssertionError("Bitmap literally can't be null wtf");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        localeIndex = localeIndex % locales.length;

    }

    public Bitmap screenShot(View view) {
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
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
