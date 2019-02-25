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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.HomeScreen;
import com.bald.uriah.baldphone.adapters.BaldPagerAdapter;
import com.bald.uriah.baldphone.views.BaldLinearLayoutButton;
import com.bald.uriah.baldphone.views.HomeScreenAppView;

public class HomeViewFactory extends HomeView {
    public static final String TAG = HomeViewFactory.class.getSimpleName();
    public static final int AMOUNT_PER_PAGE = 8;
    public ConstraintLayout child;

    public HomeViewFactory(@NonNull HomeScreen homeScreen) {
        super(homeScreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        final ConstraintLayout view = (ConstraintLayout) inflater.inflate(R.layout.home_factory, container, false);
        this.child = view;
        return view;
    }

    public void populate(int index) {
        @NonNull final BaldPagerAdapter baldPagerAdapter = homeScreen.baldPagerAdapter;

        final int startIndex = AMOUNT_PER_PAGE * (index);
        int endIndex = ((AMOUNT_PER_PAGE * (index)) + AMOUNT_PER_PAGE);
        if (endIndex > baldPagerAdapter.pinnedList.size())
            endIndex = baldPagerAdapter.pinnedList.size();


        for (int i = 0; i < endIndex - startIndex; i++) {
            final HomeScreenAppView homeScreenAppView = new HomeScreenAppView(
                    (BaldLinearLayoutButton) child.getChildAt(
                            i / 2
                                    +
                                    (((i % 2)) * AMOUNT_PER_PAGE / 2))
            );
            homeScreenAppView.setVisibility(VISIBLE);
            baldPagerAdapter.pinnedList.get(i + startIndex).applyToHomeScreenAppView(homeScreenAppView);
        }
    }

    public void recycle() {
        for (int i = 0; i < child.getChildCount(); i++) {
            final View view = child.getChildAt(i);
            view.setVisibility(View.INVISIBLE);
            view.setOnClickListener(null);
        }
    }
}

