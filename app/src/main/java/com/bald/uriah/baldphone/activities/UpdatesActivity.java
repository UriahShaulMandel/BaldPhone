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
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.UpdatingUtil;

import java.io.File;

public class UpdatesActivity extends BaldActivity {
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        final String[] message = getIntent().getStringArrayExtra(EXTRA_MESSAGE);
        findViewById(R.id.tv_current_version);

        ((TextView) findViewById(R.id.tv_new_version)).append(message[1]);
        ((TextView) findViewById(R.id.tv_current_version)).append(BuildConfig.VERSION_NAME);
        ((TextView) findViewById(R.id.tv_change_log)).setText(message[2]);

        final SharedPreferences sharedPreferences = BPrefs.get(this);
        final int downloadedVersion = sharedPreferences.getInt(BPrefs.LAST_APK_VERSION_KEY, -1);
        final int newVersion = Integer.parseInt(message[0]);
        final TextView bt = findViewById(R.id.bt);
        if (downloadedVersion == newVersion && new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), UpdatingUtil.FILENAME).exists()) {
            bt.setOnClickListener(v -> UpdatingUtil.install(this));
            bt.setText(R.string.install);
        } else {
            bt.setOnClickListener(v -> {
                if (((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                        .isConnected()) {
                    UpdatingUtil.downloadApk(this, newVersion);


                } else {
                    BDB.from(this)
                            .setTitle(R.string.data_warning)
                            .setSubText(R.string.data_warning_subtext)
                            .setDialogState(BDialog.DialogState.YES_CANCEL)
                            .setCancelable(true)
                            .setPositiveButtonListener(params -> {
                                UpdatingUtil.downloadApk(this, newVersion);

                                return true;
                            })
                            .show();
                }
            });
        }
    }


    @Override
    protected int requiredPermissions() {
        return PERMISSION_REQUEST_INSTALL_PACKAGES | PERMISSION_WRITE_EXTERNAL_STORAGE;
    }
}
