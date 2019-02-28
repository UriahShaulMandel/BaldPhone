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

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.UpdatingUtil;

import java.io.File;

public class UpdatesActivity extends BaldActivity {
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private String[] message;
    private BaldToast notConnected, couldNotStartDownload;
    private TextView tv_new_version, tv_current_version, tv_change_log, bt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        message = getIntent().getStringArrayExtra(EXTRA_MESSAGE);
        if (!UpdatingUtil.isMessageOk(message)) {
            BaldToast.error(this);
            finish();
            return;
        }

        tv_new_version = findViewById(R.id.tv_new_version);
        tv_current_version = findViewById(R.id.tv_current_version);
        tv_change_log = findViewById(R.id.tv_change_log);
        bt = findViewById(R.id.bt);

        notConnected = BaldToast.from(this).setType(BaldToast.TYPE_ERROR).setText(R.string.could_not_connect_to_internet).build();
        couldNotStartDownload = BaldToast.from(this).setType(BaldToast.TYPE_ERROR).setText(R.string.could_not_start_the_download).build();

        apply();
    }

    public void apply() {
        if (isDestroyed())
            return;
        tv_new_version.setText(String.format("%s%s", getString(R.string.new_version), message[1]));
        tv_current_version.setText(String.format("%s%s", getString(R.string.current_version), BuildConfig.VERSION_NAME));
        tv_change_log.setText(message[2]);

        final SharedPreferences sharedPreferences = BPrefs.get(this);
        final int downloadedVersion = sharedPreferences.getInt(BPrefs.LAST_APK_VERSION_KEY, -1);
        final int newVersion = Integer.parseInt(message[0]);

        if (downloadedVersion == newVersion && new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), UpdatingUtil.FILENAME).exists()) {
            bt.setOnClickListener(v -> UpdatingUtil.install(this));
            bt.setText(R.string.install);
        } else {
            bt.setOnClickListener(v -> {
                final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    final NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (networkInfo != null) {
                        if (networkInfo.isConnected()) {
                            onDownloadButtonClick(newVersion);
                        } else {
                            BDB.from(this)
                                    .setTitle(R.string.data_warning)
                                    .setSubText(R.string.data_warning_subtext)
                                    .setDialogState(BDialog.DialogState.YES_CANCEL)
                                    .setCancelable(true)
                                    .setPositiveButtonListener(params -> {
                                        onDownloadButtonClick(newVersion);
                                        return true;
                                    })
                                    .show();
                        }
                    } else {
                        notConnected.show();
                    }
                } else {
                    notConnected.show();
                }
            });
        }
    }

    private void onDownloadButtonClick(final int newVersion) {
        if (UpdatingUtil.downloadApk(this, newVersion)) {
            bt.setText(R.string.downloading);
            bt.setOnClickListener(D.EMPTY_CLICK_LISTENER);
        } else {
            couldNotStartDownload.show();
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_REQUEST_INSTALL_PACKAGES | PERMISSION_WRITE_EXTERNAL_STORAGE;
    }
}
