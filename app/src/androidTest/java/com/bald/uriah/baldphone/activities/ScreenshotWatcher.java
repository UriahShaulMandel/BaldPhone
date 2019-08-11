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

import android.graphics.Bitmap;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.screenshot.ScreenCapture;
import androidx.test.runner.screenshot.Screenshot;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.util.Locale;

public class ScreenshotWatcher extends TestWatcher {

    @Override
    protected void succeeded(Description description) {
        Locale locale = InstrumentationRegistry.getInstrumentation().getContext()
                .getResources()
                .getConfiguration()
                .locale;

        captureScreenshot(description.getMethodName() + "_" + locale.toLanguageTag());
    }

    private void captureScreenshot(String name) {
        ScreenCapture capture = Screenshot.capture();
        capture.setFormat(Bitmap.CompressFormat.PNG);
        capture.setName(name);
        try {
            capture.process();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        captureScreenshot(description.getMethodName() + "_fail");
    }
}