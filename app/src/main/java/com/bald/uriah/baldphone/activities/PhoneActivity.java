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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.contacts.ContactsActivity;

public class PhoneActivity extends BaldActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        findViewById(R.id.bt_contacts).
                setOnClickListener(v -> startActivity(new Intent(this, ContactsActivity.class)));

        findViewById(R.id.bt_dialer).
                setOnClickListener(v -> startActivity(new Intent(this, DialerActivity.class)));

        if (BuildConfig.FLAVOR.equals("gPlay")) {
            findViewById(R.id.bt_recent).setVisibility(View.GONE);
            findViewById(R.id.div_2).setVisibility(View.GONE);
        } else
            findViewById(R.id.bt_recent).
                    setOnClickListener(v -> startActivity(new Intent(this, RecentActivity.class)));

    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
