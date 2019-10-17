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

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.HelpRecyclerViewAdapter;
import com.bald.uriah.baldphone.utils.S;

public class VideoTutorialsActivity extends BaldActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        final RecyclerView recycler_view = findViewById(R.id.recycler_view);
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recycler_view.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.ll_divider));
        recycler_view.addItemDecoration(dividerItemDecoration);
        final Resources resources = getResources();
        recycler_view.setAdapter(
                new HelpRecyclerViewAdapter(
                        this,
                        S.typedArrayToResArray(resources, R.array.yt_texts),
                        S.typedArrayToResArray(resources, R.array.yt_logos)));
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
