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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.views.ModularRecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class IntentAdapter extends ModularRecyclerView.ModularAdapter<IntentAdapter.ViewHolder> {
    public interface ResolveInfoConsumer{
        void consume(final ResolveInfo resolveInfo, final Context context);
    }

    private final LayoutInflater layoutInflater;
    private final List<ResolveInfo> resolveInfoList;
    private final PackageManager packageManager;
    private final ResolveInfoConsumer resolveInfoConsumer;
    private final Context context;

    public IntentAdapter(final Context context, final List<ResolveInfo> resolveInfoList, final ResolveInfoConsumer resolveInfoConsumer) {
        this.layoutInflater = LayoutInflater.from(context);
        this.packageManager = context.getPackageManager();
        this.resolveInfoList = resolveInfoList;
        this.resolveInfoConsumer = resolveInfoConsumer;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.settings_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.update(position);
    }

    @Override
    public int getItemCount() {
        return resolveInfoList.size();
    }

    class ViewHolder extends ModularRecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView settings_icon;
        final TextView tv_settings_name;

        public ViewHolder(final View itemView) {
            super(itemView);
            settings_icon = itemView.findViewById(R.id.setting_icon);
            tv_settings_name = itemView.findViewById(R.id.tv_setting_name);
            itemView.setOnClickListener(this);
        }

        public void update(final int position) {
            final ResolveInfo resolveInfo = resolveInfoList.get(position);
            Glide.with(settings_icon)
                    .load(resolveInfo.loadIcon(packageManager))
                    .into(settings_icon);
            tv_settings_name.setText(resolveInfo.loadLabel(packageManager));
        }

        @Override
        public void onClick(final View v) {
            resolveInfoConsumer.consume(resolveInfoList.get(getAdapterPosition()),context);
        }
    }
}
