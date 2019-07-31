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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.activities.UpdatesActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * On Different class than {@link UpdatesActivity} because may be exported to library in the future
 */
public class UpdatingUtil {
    public static final String MESSAGE_URL = "https://api.github.com/repos/UriahShaulMandel/BaldPhone/releases/latest";
    public static final String FILENAME = "BaldPhoneUpdate.apk";
    public static final String VOLLEY_TAG = "baldphone";

    @NonNull
    public static File getDownloadedFile() {
        final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        downloads.mkdir();
        return new File(downloads.getAbsoluteFile() + "/" + FILENAME);
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

    private static boolean updatePending(@NonNull BaldUpdateObject baldUpdateObject) {
        return baldUpdateObject.versionCode > BuildConfig.VERSION_CODE;
    }

    public static void checkForUpdates(BaldActivity activity, boolean retAnswer) {
        if (!UpdatingUtil.isOnline(activity) && retAnswer) {
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
                            try {
                                final BaldUpdateObject baldUpdateObject = BaldUpdateObject.parseMessage(response);
                                if (updatePending(baldUpdateObject)) {
                                    BDB.from(activity)
                                            .setTitle(R.string.pending_update)
                                            .setSubText(R.string.a_new_update_is_available)
                                            .addFlag(BDialog.FLAG_OK | BDialog.FLAG_CANCEL)
                                            .setPositiveButtonListener(params -> {
                                                activity.startActivity(
                                                        new Intent(activity, UpdatesActivity.class)
                                                                .putExtra(UpdatesActivity.EXTRA_BALD_UPDATE_OBJECT, baldUpdateObject));
                                                return true;
                                            })
                                            .setNegativeButtonListener(params -> {
                                                BPrefs.get(activity)
                                                        .edit()
                                                        .putLong(BPrefs.LAST_UPDATE_ASKED_VERSION_KEY, System.currentTimeMillis())
                                                        .apply();
                                                return true;
                                            })
                                            .show();
                                } else {
                                    if (retAnswer)
                                        BaldToast
                                                .from(activity)
                                                .setText(R.string.baldphone_is_up_to_date)
                                                .show();
                                }
                            } catch (JSONException e) {
                                if (retAnswer) {
                                    BaldToast.from(activity).setType(BaldToast.TYPE_ERROR).setText(R.string.update_message_is_corrupted).show();
                                    BaldToast.from(activity).setType(BaldToast.TYPE_ERROR).setText(S.str(e.getMessage())).show();
                                }
                            }
                            lifecycle.removeObserver(observer);
                        },
                        error -> {
                            if (retAnswer)
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

    public static class BaldUpdateObject implements Parcelable {
        public static final Creator<BaldUpdateObject> CREATOR = new Creator<BaldUpdateObject>() {
            @Override
            public BaldUpdateObject createFromParcel(Parcel in) {
                return new BaldUpdateObject(in);
            }

            @Override
            public BaldUpdateObject[] newArray(int size) {
                return new BaldUpdateObject[size];
            }
        };
        public final int versionCode;
        public final String versionName;
        public final String changeLog;
        public final String apkUrl;

        public BaldUpdateObject(int versionCode, String versionName, String changeLog, String apkUrl) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.changeLog = changeLog;
            this.apkUrl = apkUrl;
        }

        protected BaldUpdateObject(Parcel in) {
            versionCode = in.readInt();
            versionName = in.readString();
            changeLog = in.readString();
            apkUrl = in.readString();
        }

        public static BaldUpdateObject parseMessage(String json) throws JSONException {
            final JSONObject root = new JSONObject(json);
            final int versionNumber = Integer.parseInt(root.getString("tag_name"));
            final String versionName = root.getString("name");

            final JSONArray assets = root.getJSONArray("assets");
            final JSONObject apkObject = assets.getJSONObject(0);
            if (!apkObject.getString("content_type").equals("application/vnd.android.package-archive"))
                throw new JSONException("first object in assets array is not an apk file!");
            final String apkDownloadUrl = apkObject.getString("browser_download_url");
            final String changeLog = root.getString("body");

            return new BaldUpdateObject(
                    versionNumber,
                    versionName,
                    changeLog,
                    apkDownloadUrl
            );
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(versionCode);
            dest.writeString(versionName);
            dest.writeString(changeLog);
            dest.writeString(apkUrl);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}