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

package com.bald.uriah.baldphone.fragments_and_dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.IntentAdapter;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@Deprecated
public class IntentChooserDialog extends Dialog {
    private static final String TAG = IntentChooserDialog.class.getSimpleName();
    private final List<ResolveInfo> resolveInfoList;
    private final IntentAdapter.ResolveInfoConsumer resolveInfoConsumer;

    public IntentChooserDialog(@NonNull final Context context, @NonNull final List<ResolveInfo> resolveInfoList, @NonNull final IntentAdapter.ResolveInfoConsumer resolveInfoConsumer) {
        super(context);
        this.resolveInfoList = resolveInfoList;
        this.resolveInfoConsumer = resolveInfoConsumer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.full_screen_recycler_view_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ((BaldTitleBar) findViewById(R.id.bald_title_bar)).getBt_back().setOnClickListener(v -> dismiss());


        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setClipToPadding(false);
        final Point point = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(point);
        recyclerView.setAdapter(new IntentAdapter(getContext(), resolveInfoList, resolveInfoConsumer));
        recyclerView.setItemViewCacheSize(10);//In Order to cover up the shitiness of loading resolve infos icons and texts
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .drawable(R.drawable.settings_divider)
                        .build()
        );

    }

}