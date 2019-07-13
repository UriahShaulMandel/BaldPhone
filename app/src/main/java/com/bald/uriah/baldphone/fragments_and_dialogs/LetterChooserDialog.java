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
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

public class LetterChooserDialog extends Dialog {
    private static final String TAG = LetterChooserDialog.class.getSimpleName();
    private static final int AMOUNT_PER_ROW = 4;
    private final OnChooseLetterListener onChooseLetterListener;
    private final SparseIntArray lettersToValues;

    public LetterChooserDialog(final Context context, SparseIntArray lettersToValues, OnChooseLetterListener onChooseLetterListener) {
        super(context);
        this.lettersToValues = lettersToValues;
        this.onChooseLetterListener = onChooseLetterListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.full_screen_recycler_view_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ((BaldTitleBar) findViewById(R.id.bald_title_bar)).getBt_back().setOnClickListener(v -> {
            dismiss();
        });
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        int numberOfAppsInARow;
        try {
            WindowManager windowManager = getWindow().getWindowManager();
            final Point point = new Point();
            windowManager.getDefaultDisplay().getSize(point);
            numberOfAppsInARow = ((point.x / point.y) != 0) ? AMOUNT_PER_ROW * 2 : AMOUNT_PER_ROW;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            numberOfAppsInARow = 3;
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfAppsInARow));
        recyclerView.setAdapter(new LetterChooserAdapter());

    }

    @FunctionalInterface
    public interface OnChooseLetterListener {
        void onChooseLetter(int position);
    }

    public class LetterChooserAdapter extends ModularRecyclerView.ModularAdapter<LetterChooserAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.letter_chooser_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            final char c = (char) lettersToValues.keyAt(position);
            holder.textView.setText(String.valueOf(c));
            holder.textView.setOnClickListener((v) -> {
                onChooseLetterListener.onChooseLetter(lettersToValues.valueAt(position));
                cancel();
            });
        }

        @Override
        public int getItemCount() {
            return lettersToValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.text);
            }
        }
    }
}