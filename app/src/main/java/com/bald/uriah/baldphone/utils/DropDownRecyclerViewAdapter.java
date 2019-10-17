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

package com.bald.uriah.baldphone.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;

public class DropDownRecyclerViewAdapter extends RecyclerView.Adapter<DropDownRecyclerViewAdapter.ViewHolder> {
    private final LayoutInflater layoutInflater;
    private final PopupWindow popupWindow;
    private final DropDownListener dropDownListener;

    public DropDownRecyclerViewAdapter(final Context context, PopupWindow popupWindow, DropDownListener dropDownListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.popupWindow = popupWindow;
        this.dropDownListener = dropDownListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.settings_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        dropDownListener.onUpdate(holder, position, popupWindow);
    }

    @Override
    public int getItemCount() {
        return dropDownListener.size();
    }

    public interface DropDownListener {
        void onUpdate(DropDownRecyclerViewAdapter.ViewHolder viewHolder, final int position, PopupWindow popupWindow);

        int size();

        default void onDismiss() {
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView pic;
        public final TextView text;

        public ViewHolder(final View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.setting_icon);
            text = itemView.findViewById(R.id.tv_setting_name);
        }
    }
}