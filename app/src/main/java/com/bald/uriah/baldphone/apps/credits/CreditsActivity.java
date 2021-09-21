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

package com.bald.uriah.baldphone.apps.credits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.core.BaldActivity;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

import java.util.Objects;

public class CreditsActivity extends BaldActivity {
    private String[] names, tasks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        names = getResources().getStringArray(R.array.names);
        tasks = getResources().getStringArray(R.array.tasks);

        recyclerView.setAdapter(new CreditsAdapter());
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.ll_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);

    }

    class CreditsAdapter extends ModularRecyclerView.ModularAdapter<CreditsAdapter.ViewHolder> {
        final LayoutInflater inflater;

        public CreditsAdapter() {
            this.inflater = getLayoutInflater();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.credit, parent, false));
        }

        @Override
        public int getItemCount() {
            return names.length;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.name.setText(names[position]);
            holder.task.setText(tasks[position]);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView name, task;

            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                task = itemView.findViewById(R.id.task);
            }
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
