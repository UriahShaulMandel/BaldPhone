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

package com.bald.uriah.baldphone.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.util.Pools;
import com.bald.uriah.baldphone.activities.HomeScreenActivity;
import com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.views.home.HomePage1;
import com.bald.uriah.baldphone.views.home.HomePage2;
import com.bald.uriah.baldphone.views.home.HomeViewFactory;
import com.bald.uriah.baldphone.views.home.NotesView;

import java.util.Collections;
import java.util.List;

public class BaldPagerAdapter extends BaldViewAdapter {
    private static final String TAG = BaldPagerAdapter.class.getSimpleName();

    private final Pools.SimplePool<HomeViewFactory> factoryPool = new Pools.SimplePool<>(10);

    public int startingPage;
    public List<HomeScreenPinHelper.HomeScreenPinnable> pinnedList = Collections.EMPTY_LIST;
    private int numItemsBefore, numItems;
    private HomeScreenActivity homeScreen;

    public BaldPagerAdapter(HomeScreenActivity homeScreen) {
        this.homeScreen = homeScreen;
        startingPage = (this.homeScreen.getSharedPreferences(BPrefs.KEY, Context.MODE_PRIVATE).getBoolean(BPrefs.NOTE_VISIBLE_KEY, BPrefs.NOTE_VISIBLE_DEFAULT_VALUE) ? 2 : 1);
        numItems = numItemsBefore = startingPage + 1;

    }

    public void obtainAppList() {
        pinnedList = HomeScreenPinHelper.getAll(homeScreen);
        numItems =
                numItemsBefore + (pinnedList.size() / HomeViewFactory.AMOUNT_PER_PAGE + (pinnedList.size() % HomeViewFactory.AMOUNT_PER_PAGE == 0 ? 0 : 1));
        notifyDataSetChanged();
    }

    public View getItem(int position) {
        final View view;
        switch (position) {
            case -1:
                view = new NotesView(homeScreen);
                view.setTag(NotesView.TAG);
                break;
            case 0:
                view = new HomePage2(homeScreen);
                view.setTag(HomePage2.TAG);
                break;
            case 1:
                view = new HomePage1(homeScreen);
                view.setTag(HomePage1.TAG);
                break;
            default:
                HomeViewFactory homeFragmentFactory = factoryPool.acquire();
                if (homeFragmentFactory == null)
                    homeFragmentFactory = new HomeViewFactory(homeScreen);
                view = homeFragmentFactory;
                ((HomeViewFactory) view).populate(position - 2);
                view.setTag(HomeViewFactory.TAG + (position - 2));
                break;
        }
        return view;
    }

    @Override
    public int getCount() {
        return numItems;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final int virtualPosition = position - (startingPage - 1);
        return super.instantiateItem(container, virtualPosition);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        final int virtualPosition = position - (startingPage - 1);
        super.destroyItem(container, virtualPosition, object);
        if (object instanceof HomeViewFactory) {
            final HomeViewFactory homeFragmentFactory = (HomeViewFactory) object;
            homeFragmentFactory.recycle();
            factoryPool.release(homeFragmentFactory);
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return object instanceof HomeViewFactory ? POSITION_NONE : super.getItemPosition(object);
    }
}
