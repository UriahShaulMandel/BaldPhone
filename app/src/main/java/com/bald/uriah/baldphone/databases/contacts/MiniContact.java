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

package com.bald.uriah.baldphone.databases.contacts;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper;
import com.bald.uriah.baldphone.utils.Constants;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.HomeScreenAppView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * Mini contact, contains lookupkey,photo,name and id.
 */
public class MiniContact implements HomeScreenPinHelper.HomeScreenPinnable, Constants.BaseContactsConstants {

    public final String lookupKey, photo;
    @Nullable
    public final String name;
    public final int id;
    public final boolean favorite;

    public MiniContact(String lookupKey, @Nullable String name, String photo, int id, boolean favorite) {
        this.lookupKey = lookupKey;
        this.name = name;
        this.photo = photo;
        this.id = id;
        this.favorite = favorite;
    }

    @Override
    public void applyToHomeScreenAppView(HomeScreenAppView homeScreenAppView) {
        if (S.isValidContextForGlide(homeScreenAppView.iv_icon.getContext()))
            Glide
                    .with(homeScreenAppView.iv_icon)
                    .load(photo)
                    .apply(new RequestOptions()
                            .error(R.drawable.face_on_button))
                    .into(homeScreenAppView.iv_icon);
        homeScreenAppView.setText(name);
        homeScreenAppView.setIntent(lookupKey);
    }
}