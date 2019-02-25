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

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.databases.apps.AppsDatabase;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.S;

import java.io.File;

public class TechnicalInfoActivity extends BaldActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        setContentView(R.layout.activity_tech_info);
        ((TextView) findViewById(R.id.tv_info)).setText(getTechnicalInfo());
        findViewById(R.id.bt_clear_cache).setOnClickListener(v -> {
            BDB.from(this)
                    .setDialogState(BDialog.DialogState.YES_CANCEL)
                    .setTitle(R.string.clear_cache)
                    .setSubText(R.string.clear_cache_subtext)
                    .setPositiveButtonListener(params -> {
                        deleteCache(this);
                        BaldToast.simple(this, R.string.cache_cleared_successfully);
                        return true;
                    })
                    .show();


        });

        findViewById(R.id.bt_clear_data).

                setOnClickListener(v ->

                {
                    BDB.from(this)
                            .setDialogState(BDialog.DialogState.YES_CANCEL)
                            .setTitle(R.string.clear_data)
                            .setSubText(R.string.clear_data_subtext)
                            .setPositiveButtonListener(params -> {
                                BDB.from(this)
                                        .setDialogState(BDialog.DialogState.YES_CANCEL)
                                        .setTitle(R.string.clear_data)
                                        .setSubText(R.string.clear_data_subtext2)
                                        .setPositiveButtonListener(params2 -> {
                                            BDB.from(this)
                                                    .setDialogState(BDialog.DialogState.YES_CANCEL)
                                                    .setTitle(R.string.clear_data)
                                                    .setSubText(R.string.clear_data_subtext3)
                                                    .setPositiveButtonListener(params3 -> {
                                                        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                                                        return true;
                                                    }).show();
                                            return true;
                                        })
                                        .show();
                                return true;
                            })
                            .show();
                });
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_WRITE_SETTINGS | PERMISSION_WRITE_EXTERNAL_STORAGE;
    }


    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppsDatabase.getInstance(context).appsDatabaseDao().deleteAll();
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static String getTechnicalInfo() {
        return
                String.format("Api Level: %s\nVersion Name: %s\nVersion Code: %s\nManufacturer: %s\nBrand: %s\nDevice: %s\nModel: %s\n",
                        S.str(Build.VERSION.SDK_INT),
                        S.str(BuildConfig.VERSION_NAME),
                        S.str(BuildConfig.VERSION_CODE),
                        S.str(Build.MANUFACTURER),
                        S.str(Build.BRAND),
                        S.str(Build.DEVICE),
                        S.str(Build.MODEL))
                ;
    }
}
