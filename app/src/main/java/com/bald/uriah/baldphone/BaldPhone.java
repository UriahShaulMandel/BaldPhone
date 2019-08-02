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

package com.bald.uriah.baldphone;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bald.uriah.baldphone.activities.UpdatesActivity;
import com.bald.uriah.baldphone.databases.alarms.AlarmScheduler;
import com.bald.uriah.baldphone.databases.reminders.ReminderScheduler;
import com.bald.uriah.baldphone.services.NotificationListenerService;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldUncaughtExceptionHandler;
import com.bald.uriah.baldphone.utils.S;

import net.danlew.android.joda.JodaTimeAndroid;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import java.util.Locale;
import java.util.UUID;

public class BaldPhone extends Application {
    private static final String TAG = BaldPhone.class.getSimpleName();
    private static final String SERVER_URL = "http://baldphone.co.nf/insert_new_install.php?uuid=%s&vcode=%d";
    private static final String VOLLEY_TAG = "baldphone_server";

    // Application class should not have any fields, http://www.developerphil.com/dont-store-data-in-the-application-object/

    @Override
    public void onCreate() {
        S.logImportant("BaldPhone was started!");
        super.onCreate();
        JodaTimeAndroid.init(this);
        AlarmScheduler.reStartAlarms(this);
        ReminderScheduler.reStartReminders(this);
        if (BuildConfig.FLAVOR.equals("baldUpdates")) {
            UpdatesActivity.removeUpdatesInfo(this);
        }
        try {
            startService(new Intent(this, NotificationListenerService.class));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        sendVersionInfo();
    }

    private void sendVersionInfo() {
        if (S.isEmulator())
            return;

        final SharedPreferences sharedPreferences = BPrefs.get(this);
        if (!sharedPreferences.contains(BPrefs.UUID_KEY)) {
            sharedPreferences.edit().putString(BPrefs.UUID_KEY, UUID.randomUUID().toString()).apply();
        }

        Volley.newRequestQueue(this).add(
                new StringRequest(
                        Request.Method.GET,
                        String.format(Locale.US, SERVER_URL, sharedPreferences.getString(BPrefs.UUID_KEY, null), BuildConfig.VERSION_CODE),
                        response -> {
                        },
                        error -> {
                        }
                ).setTag(VOLLEY_TAG));
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        final CoreConfigurationBuilder builder =
                new CoreConfigurationBuilder(this)
                        .setBuildConfigClass(BuildConfig.class)
                        .setReportFormat(StringFormat.JSON);
        builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder.class)
                .setUri(getString(R.string.tt_url))
                .setHttpMethod(HttpSender.Method.POST)
                .setEnabled(true);
        ACRA.init(this, builder);

        Thread.setDefaultUncaughtExceptionHandler(
                new BaldUncaughtExceptionHandler(this, Thread.getDefaultUncaughtExceptionHandler())
        );
    }
}