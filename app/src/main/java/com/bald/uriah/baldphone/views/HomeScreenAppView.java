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

package com.bald.uriah.baldphone.views;

import android.content.ComponentName;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.apps.contacts.SingleContactActivity;
import com.bald.uriah.baldphone.utils.S;

public class HomeScreenAppView {
    public final ImageView iv_icon;
    private final BaldLinearLayoutButton child;
    private final TextView tv_name;

    public HomeScreenAppView(BaldLinearLayoutButton child) {
        this.child = child;
        tv_name = child.findViewById(R.id.et_name);
        iv_icon = child.findViewById(R.id.iv_icon);
    }

    public void setText(@StringRes int resId) {
        tv_name.setText(resId);
    }

    public void setText(CharSequence charSequence) {
        tv_name.setText(charSequence);
    }

    public void setIntent(final ComponentName componentName) {
        child.setOnClickListener(v -> S.startComponentName(v.getContext(), componentName));
    }

    public void setIntent(final String contactLookupKey) {
        child.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), SingleContactActivity.class).putExtra(SingleContactActivity.CONTACT_LOOKUP_KEY, contactLookupKey)));
    }

    public void setVisibility(int visibility) {
        child.setVisibility(visibility);
    }
}
