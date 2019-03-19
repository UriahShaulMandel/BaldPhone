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

package com.bald.uriah.baldphone.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.activities.UpdatesActivity;

import java.io.File;

/**
 * On Different class than {@link UpdatesActivity} because may be exported to library in the future
 */
public class UpdatingUtil {
    public static final String MESSAGE_URL = "https://raw.githubusercontent.com/UriahShaulMandel/BaldPhone/master/apks/last_release.txt";
    public static final String APK_URL = "https://github.com/UriahShaulMandel/BaldPhone/blob/master/apks/app-release.apk?raw=true";
    public static final String FILENAME = "BaldPhoneUpdate.apk";
    public static final String VOLLEY_TAG = "baldphone";
    public static final String divider = "@@@";
    public static final int MESSAGE_PARTS = 3;

    public static final int
            MESSAGE_VERSION_CODE = 0,
            MESSAGE_VERSION_NAME = 1,
            MESSAGE_VERSION_CHANGE_LOG = 2,
            MESSAGE_ALTERNATIVE_URL = 3;
    public static final String NO_ALTERNATIVE_URL = "NO_ALTERNATIVE_URL";

    @NonNull
    public static File getDownloadedFile() {
        final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        downloads.mkdir();
        return new File(downloads.getAbsoluteFile() + "/" + FILENAME);
    }


    public static boolean isMessageOk(String message) {
        if (message == null || message.length() == 0 || !message.contains(divider))
            return false;
        final String[] arr = message.split(divider);
        return isMessageOk(arr);
    }

    public static boolean isMessageOk(String[] message) {
        return message.length >= MESSAGE_PARTS && android.text.TextUtils.isDigitsOnly(message[MESSAGE_VERSION_CODE]);
    }

    public static boolean isOnline(Context context) {
        final ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    public static boolean updatePending(@NonNull String[] message) {
        return Integer.parseInt(message[MESSAGE_VERSION_CODE]) > BuildConfig.VERSION_CODE;
    }

    public static void checkForUpdates(BaldActivity activity) {
        if (!UpdatingUtil.isOnline(activity)) {
            BaldToast.from(activity)
                    .setType(BaldToast.TYPE_ERROR)
                    .setText(R.string.could_not_connect_to_server)
                    .setLength(1)
                    .show();
            return;
        }
        final RequestQueue queue = Volley.newRequestQueue(activity);
        final Lifecycle lifecycle = activity.getLifecycle();
        final LifecycleObserver observer = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void releaseRequest() {
                queue.cancelAll(VOLLEY_TAG);
                lifecycle.removeObserver(this);
            }
        };
        queue.add(
                new StringRequest(
                        Request.Method.GET,
                        MESSAGE_URL,
                        response -> {
                            if (isMessageOk(response)) {
                                final String[] message = response.split(divider);
                                if (updatePending(message)) {
                                    BDB.from(activity)
                                            .setTitle(R.string.pending_update)
                                            .setSubText(R.string.a_new_update_is_available)
                                            .setCancelable(true)
                                            .setDialogState(BDialog.DialogState.OK_CANCEL)
                                            .setPositiveButtonListener(params -> {
                                                activity.startActivity(
                                                        new Intent(activity, UpdatesActivity.class)
                                                                .putExtra(UpdatesActivity.EXTRA_MESSAGE, message));
                                                return true;
                                            })
                                            .setCancelButtonListener(params -> {
                                                BPrefs.get(activity)
                                                        .edit()
                                                        .putLong(BPrefs.LAST_UPDATE_ASKED_VERSION_KEY, System.currentTimeMillis())
                                                        .apply();
                                                return true;
                                            })
                                            .show();
                                } else {
                                    BaldToast
                                            .from(activity)
                                            .setText(R.string.baldphone_is_up_to_date)
                                            .show();
                                }
                            } else {
                                BaldToast.from(activity).setType(BaldToast.TYPE_ERROR).setText(R.string.update_message_is_corrupted).show();
                            }
                            lifecycle.removeObserver(observer);
                        },
                        error -> {
                            BaldToast.from(activity)
                                    .setLength(1)
                                    .setType(BaldToast.TYPE_ERROR)
                                    .setText(R.string.could_not_connect_to_server)
                                    .show();
                            lifecycle.removeObserver(observer);
                        }
                ).setTag(VOLLEY_TAG));
        lifecycle.addObserver(observer);
    }


}