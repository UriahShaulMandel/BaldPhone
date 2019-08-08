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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.fragments_and_dialogs.tutorial_fragments.TutorialFragment1;
import com.bald.uriah.baldphone.fragments_and_dialogs.tutorial_fragments.TutorialFragment2;
import com.bald.uriah.baldphone.fragments_and_dialogs.tutorial_fragments.TutorialFragment3;
import com.bald.uriah.baldphone.fragments_and_dialogs.tutorial_fragments.TutorialFragment4;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.views.ViewPagerHolder;

public class TutorialActivity extends BaldActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_activity);
        getSharedPreferences(BPrefs.KEY, MODE_PRIVATE).edit().putBoolean(BPrefs.AFTER_TUTORIAL_KEY, true).apply();
        ViewPagerHolder viewPagerHolder = findViewById(R.id.view_pager_holder);
        viewPagerHolder.setViewPagerAdapter(new Adapter(getSupportFragmentManager()));
    }

    private static class Adapter extends FragmentPagerAdapter {
        Adapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case 0:
                    return new TutorialFragment1();
                case 1:
                    return new TutorialFragment2();
                case 2:
                    return new TutorialFragment3();
                case 3:
                    return new TutorialFragment4();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}