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

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.HomeScreen;
import com.bald.uriah.baldphone.activities.SettingsActivity;
import com.bald.uriah.baldphone.activities.VideoTutorialsActivity;


public class HomePage2 extends HomeView {
    public static final String TAG = HomePage2.class.getSimpleName();
    private View view;
    private View bt_settings, bt_internet, bt_maps, bt_help;

    public HomePage2(@NonNull HomeScreen homeScreen) {
        super(homeScreen);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.fragment_home_page2, container, false);
        attachXml();
        genOnLongClickListeners();
        return view;
    }

    private void attachXml() {
        bt_settings = view.findViewById(R.id.bt_settings);
        bt_internet = view.findViewById(R.id.bt_apps);
        bt_maps = view.findViewById(R.id.bt_maps);
        bt_help = view.findViewById(R.id.bt_help);

    }

    private void genOnLongClickListeners() {
        bt_settings.setOnClickListener(v ->
                homeScreen.startActivity(new Intent(getContext(), SettingsActivity.class)));
        bt_internet.setOnClickListener(v ->
                homeScreen.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))));
        bt_maps.setOnClickListener(v ->
                homeScreen.startActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MAPS)));
        bt_help.setOnClickListener(v ->
                homeScreen.startActivity(new Intent(getContext(), VideoTutorialsActivity.class)));

    }
}