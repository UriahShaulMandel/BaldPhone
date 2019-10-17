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

package com.bald.uriah.baldphone.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;

/**
 * A simple holder to {@link com.bald.uriah.baldphone.fragments_and_dialogs.tutorial_fragments.TutorialFragment2}
 */
public class AccessibilityLevelChangerActivity extends BaldActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accesibilty_changer);
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}