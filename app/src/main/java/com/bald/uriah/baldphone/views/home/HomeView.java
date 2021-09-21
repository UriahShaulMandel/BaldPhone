/*
 * Copyright 2019 Uriah Shaul Mandel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bald.uriah.baldphone.views.home;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.bald.uriah.baldphone.apps.homescreen.HomeScreenActivity;

public abstract class HomeView extends FrameLayout {
    protected final HomeScreenActivity homeScreen;
    protected final Activity activity;

    public HomeView(HomeScreenActivity homeScreen, Activity activity) {
        super(homeScreen == null ? activity : homeScreen);
        this.homeScreen = homeScreen;
        this.activity = activity;
        addView(onCreateView(LayoutInflater.from(activity), this), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public abstract View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup);

}
