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
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.YoutubeActivity;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

public class HelpRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<HelpRecyclerViewAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    @StringRes
    private final int[] texts;
    @DrawableRes
    private final int[] pics;

    public HelpRecyclerViewAdapter(@NonNull final Context context, @StringRes final int[] texts, @DrawableRes final int[] pics) {
        inflater = LayoutInflater.from(context);
        this.pics = pics;
        this.texts = texts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.help_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.update(texts[position], pics[position], position);//may change position in future
    }

    @Override
    public int getItemCount() {
        return texts.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView pic;
        final TextView text;
        int index;

        public ViewHolder(final View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.pic);
            text = itemView.findViewById(R.id.text);
            itemView.setOnClickListener(v -> {
                final Context context = v.getContext();
                context.startActivity(new Intent(context, YoutubeActivity.class).putExtra(YoutubeActivity.EXTRA_ID, index));
            });
        }

        public void update(@StringRes final int textRes, @DrawableRes final int picRes, int index) {
            pic.setImageResource(picRes);
            text.setText(textRes);
            this.index = index;
        }
    }
}

