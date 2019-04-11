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

package com.bald.uriah.baldphone.views.home;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.HomeScreen;
import com.bald.uriah.baldphone.activities.SettingsActivity;
import com.bald.uriah.baldphone.activities.VideoTutorialsActivity;
import com.bald.uriah.baldphone.utils.S;


public class HomePage2 extends HomeView {
    public static final String TAG = HomePage2.class.getSimpleName();
    private View view;
    private ImageView iv_internet, iv_maps;
    private View bt_settings, bt_internet, bt_maps, bt_help;
    private PackageManager packageManager;

    public HomePage2(@NonNull HomeScreen homeScreen) {
        super(homeScreen);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.fragment_home_page2, container, false);
        packageManager = homeScreen.getPackageManager();
        attachXml();
        genOnLongClickListeners();
        return view;
    }

    private void attachXml() {
        bt_settings = view.findViewById(R.id.bt_settings);
        bt_internet = view.findViewById(R.id.bt_apps);
        bt_maps = view.findViewById(R.id.bt_maps);
        iv_internet = view.findViewById(R.id.iv_internet);
        iv_maps = view.findViewById(R.id.iv_maps);

        bt_help = view.findViewById(R.id.bt_help);

    }

    private void genOnLongClickListeners() {
        bt_settings.setOnClickListener(v ->
                homeScreen.startActivity(new Intent(getContext(), SettingsActivity.class)));

        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"));
        ComponentName browserComponentName = browserIntent.resolveActivity(packageManager);
        if (browserComponentName.getClassName().equals("com.android.internal.app.ResolverActivity")) {
            bt_internet.setOnClickListener(v -> homeScreen.startActivity(browserIntent));
        } else {
            bt_internet.setOnClickListener(v -> homeScreen.startActivity(packageManager.getLaunchIntentForPackage(browserComponentName.getPackageName())));
            try {
                final ActivityInfo activityInfo = packageManager.getActivityInfo(browserComponentName, PackageManager.MATCH_DEFAULT_ONLY);
                final Drawable drawable = activityInfo.loadIcon(packageManager);
                iv_internet.setImageDrawable(drawable);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, S.str(e.getMessage()));
                e.printStackTrace();
            }
        }


        final Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
        ComponentName mapsComponentName = mapsIntent.resolveActivity(packageManager);
        if (mapsComponentName.getClassName().equals("com.android.internal.app.ResolverActivity")) {
            bt_maps.setOnClickListener(v -> homeScreen.startActivity(mapsIntent));
        } else {
            bt_maps.setOnClickListener(v -> homeScreen.startActivity(packageManager.getLaunchIntentForPackage(mapsComponentName.getPackageName())));
            try {
                final ActivityInfo activityInfo = packageManager.getActivityInfo(mapsComponentName, PackageManager.MATCH_DEFAULT_ONLY);
                final Drawable drawable = activityInfo.loadIcon(packageManager);
                iv_maps.setImageDrawable(drawable);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, S.str(e.getMessage()));
                e.printStackTrace();
            }
        }


        bt_help.setOnClickListener(v ->
                homeScreen.startActivity(new Intent(getContext(), VideoTutorialsActivity.class)));

    }
}