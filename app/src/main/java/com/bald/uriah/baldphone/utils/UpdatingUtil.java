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

import android.app.DownloadManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
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

    @NonNull
    private static File getDownloadedFile() {
        final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        downloads.mkdir();
        return new File(downloads.getAbsoluteFile() + "/" + FILENAME);
    }

    private static void deleteCurrentUpdateFile(final BaldActivity activity) {
        final File bp = getDownloadedFile();
        if (bp.exists()) {
            if (!bp.delete())
                BaldToast.from(activity)
                        .setType(BaldToast.TYPE_ERROR)
                        .setText(R.string.downloaded_update_file_could_not_be_deleted)
                        .show();
        }
    }

    /**
     * @param activity
     * @param versionNumber version number
     * @return true if download was started;
     */
    public static boolean downloadApk(final UpdatesActivity activity, final int versionNumber) {
        final DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null)
            return false;

        BaldToast.from(activity)
                .setText(R.string.downloading)
                .show();
        deleteCurrentUpdateFile(activity);
        final DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(APK_URL))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FILENAME);

        final long id = manager.enqueue(request);
        final BroadcastReceiver downloadFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (id == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                    BPrefs.get(activity)
                            .edit()
                            .putInt(BPrefs.LAST_APK_VERSION_KEY, versionNumber)
                            .apply();
                    activity.apply();
                }
            }
        };
        activity.registerReceiver(downloadFinishedReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        activity.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                activity.unregisterReceiver(downloadFinishedReceiver);
            }
        });
        return true;
    }

    public static boolean isMessageOk(String message) {
        if (message == null || message.length() == 0 || !message.contains(divider))
            return false;
        final String[] arr = message.split(divider);
        return isMessageOk(arr);
    }

    public static boolean isMessageOk(String[] message) {
        return message.length >= MESSAGE_PARTS && android.text.TextUtils.isDigitsOnly(message[0]);
    }

    private static boolean isOnline(Context context) {
        final ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    private static boolean updatePending(@NonNull String[] message) {
        return Integer.parseInt(message[0]) > BuildConfig.VERSION_CODE;
    }

    public static void checkForUpdates(BaldActivity activity) {
        if (!isOnline(activity)) {
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
                                            .setDialogState(BDialog.DialogState.YES_CANCEL)
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
                                BaldToast.from(activity)
                                        .setType(BaldToast.TYPE_ERROR)
                                        .setText(R.string.update_message_is_corrupted)
                                        .show();
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

    public static void install(final BaldActivity activity) {
        final File downloadedFile = getDownloadedFile();
        final Uri apkUri = S.fileToUriCompat(downloadedFile, activity);
        final Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE)
                    .setData(apkUri)
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(apkUri, "application/vnd.android.package-archive")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }
}