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

package com.bald.uriah.baldphone.broadcast_receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.bald.uriah.baldphone.utils.BPrefs;

public class DownloadManagerReceiver extends BroadcastReceiver {

    public static void changeToDownloadedState(Context context) {
        final SharedPreferences sharedPreferences = BPrefs.get(context);
        final int LAST_APK_VERSION_KEY = sharedPreferences.getInt(BPrefs.LAST_DOWNLOAD_MANAGER_REQUEST_VERSION_NUMBER, -1);
        sharedPreferences.edit()
                .putInt(BPrefs.LAST_APK_VERSION_KEY, LAST_APK_VERSION_KEY)
                .remove(BPrefs.LAST_DOWNLOAD_MANAGER_REQUEST_ID)
                .remove(BPrefs.LAST_DOWNLOAD_MANAGER_REQUEST_VERSION_NUMBER)
                .apply();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences sharedPreferences = BPrefs.get(context);
        if (sharedPreferences.getLong(BPrefs.LAST_DOWNLOAD_MANAGER_REQUEST_ID, -3) == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -2) && sharedPreferences.contains(BPrefs.LAST_DOWNLOAD_MANAGER_REQUEST_VERSION_NUMBER)) {
            changeToDownloadedState(context);
        }
    }
}
