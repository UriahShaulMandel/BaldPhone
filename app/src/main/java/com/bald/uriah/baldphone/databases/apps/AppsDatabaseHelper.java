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

package com.bald.uriah.baldphone.databases.apps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.utils.S;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

/**
 * static class of useful methods when using the Apps Database
 */
public class AppsDatabaseHelper {
    private static final String TAG = AppsDatabaseHelper.class.getSimpleName();

    private static List<String> getInstalledAppsFlattenComponentNames(Context context) {
        final PackageManager pm = context.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        final List<String> componentNames = new ArrayList<>(resolveInfos.size());

        ActivityInfo activityInfo;
        ComponentName componentName;

        for (int i = 0; i < resolveInfos.size(); i++) {
            activityInfo = resolveInfos.get(i).activityInfo;
            componentName = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
            if (componentName.getPackageName().equals(BuildConfig.APPLICATION_ID))
                continue;
            componentNames.add(componentName.flattenToString());
        }
        return componentNames;
    }


    /**
     * Updates Apps Database - should never throw any exception
     */
    public static void updateDB(Context context) {

        final AppsDatabase appsDatabase = AppsDatabase.getInstance(context);
        final int dbAmountBefore = appsDatabase.appsDatabaseDao().getNumberOfRows();
        final List<String> addList = new ArrayList<>();
        final List<String> realApps = getInstalledAppsFlattenComponentNames(context);

        int realAppsInDbCount = 0;

        for (String componentName : realApps) {
            if (appsDatabase.appsDatabaseDao().findByFlattenComponentName(componentName) == null) {
                addList.add(componentName);
            } else {
                ++realAppsInDbCount;
            }
        }

        if (addList.size() > 0) {
            final PackageManager packageManager = context.getPackageManager();
            final App[] appsToAdd = new App[addList.size()];
            int counter = 0;
            for (String componentName : addList) {
                try {
                    final App app = new App();
                    appsToAdd[counter++] = app;
                    app.setFlattenComponentName(componentName);
                    final ActivityInfo activityInfo =
                            packageManager.getActivityInfo(ComponentName.unflattenFromString(componentName), PackageManager.MATCH_DEFAULT_ONLY);
                    app.setLabel(String.valueOf(activityInfo.loadLabel(packageManager)));
                    final Drawable drawable = activityInfo.loadIcon(packageManager);
                    if (drawable instanceof BitmapDrawable)
                        app.setIcon(S.bitmapToByteArray(((BitmapDrawable) drawable).getBitmap()));
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        app.setIcon(S.bitmapToByteArray(S.getBitmapFromDrawable(drawable)));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                    Crashlytics.logException(new RuntimeException("cannot happen! new app is not found", e));
                }

            }
            appsDatabase.appsDatabaseDao().insertAll(appsToAdd);
        }


        if (dbAmountBefore > realAppsInDbCount) {
            final int howMuchToGo = dbAmountBefore - realAppsInDbCount;
            final int[] idsToDelete = new int[howMuchToGo];

            int idsToDeleteCounter = 0;
            boolean shouldDelete;

            for (App app : appsDatabase.appsDatabaseDao().getAll()) {
                shouldDelete = true;
                for (String componentName : realApps)
                    if (app.getFlattenComponentName().equals(componentName)) {
                        shouldDelete = false;
                        break;
                    }
                if (shouldDelete)
                    idsToDelete[idsToDeleteCounter++] = app.getId();

            }
            appsDatabase.appsDatabaseDao().deleteByIds(idsToDelete);
        }
    }
}
