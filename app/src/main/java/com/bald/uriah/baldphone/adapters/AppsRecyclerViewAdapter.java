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

package com.bald.uriah.baldphone.adapters;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.apps.applications.AppsActivity;
import com.bald.uriah.baldphone.core.BaldActivity;
import com.bald.uriah.baldphone.databases.apps.App;
import com.bald.uriah.baldphone.databases.apps.AppsDatabaseHelper;
import com.bald.uriah.baldphone.fragments_and_dialogs.LetterChooserDialog;
import com.bald.uriah.baldphone.core.BPrefs;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppsRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public final float elevation;
    public final List<InAppsRecyclerView> dataList;
    @ColorInt
    private final int textColorOnSelected, textColorOnBackground;
    private final Drawable selectedDrawable;
    private final AppsActivity.ChangeAppListener changeAppListener;
    private final RecyclerView caller;
    private final BaldActivity activity;
    private final LayoutInflater layoutInflater;
    private final SparseIntArray letterToPosition;
    public int index = -1;
    public AppViewHolder lastView;
    private boolean appsOneGrid;

    public AppsRecyclerViewAdapter(List<App> appList, BaldActivity activity, AppsActivity.ChangeAppListener changeAppListener, RecyclerView caller) {
        this.caller = caller;
        this.activity = activity;
        this.layoutInflater = LayoutInflater.from(activity);
        selectedDrawable = ContextCompat.getDrawable(activity, R.drawable.btn_selected);
        elevation =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        activity.getResources().getDisplayMetrics());
        this.changeAppListener = changeAppListener;
        this.appsOneGrid = BPrefs.get(activity).getBoolean(BPrefs.APPS_ONE_GRID_KEY, BPrefs.APPS_ONE_GRID_DEFAULT_VALUE);
        letterToPosition = new SparseIntArray();
        dataList = new ArrayList<>((int) (appList.size() * 1.5));
        String lastChar = "";
        String disChar;
        for (int i = 0; i < appList.size(); i++) {
            disChar = appList.get(i).getLabel().substring(0, 1).toUpperCase();
            if (!appsOneGrid && !disChar.equals(lastChar)) {
                dataList.add(new AppStickyHeader(disChar));
                letterToPosition.append(disChar.charAt(0), dataList.size() - 1);
            }
            dataList.add(appList.get(i));
            lastChar = disChar;
        }

        final TypedValue typedValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        theme.resolveAttribute(R.attr.bald_text_on_selected, typedValue, true);
        textColorOnSelected = typedValue.data;
        theme.resolveAttribute(R.attr.bald_text_on_background, typedValue, true);
        textColorOnBackground = typedValue.data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            final View view = layoutInflater.inflate(R.layout.apps_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            final View view = layoutInflater.inflate(R.layout.app_icon, parent, false);
            return new AppViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        if (getItemViewType(position) == TYPE_ITEM) ((AppViewHolder) holder).update(position);
        else ((HeaderViewHolder) holder).update(position);
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).type();
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface InAppsRecyclerView {
        default int type() {
            return TYPE_HEADER;
        }
    }

    public static class AppStickyHeader implements InAppsRecyclerView {
        public final String name;

        AppStickyHeader(String name) {
            this.name = name;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView tv_letter;

        HeaderViewHolder(View itemView) {
            super(itemView);
            this.tv_letter = itemView.findViewById(R.id.tv_letter);
            tv_letter.setOnClickListener((v) -> {
                LetterChooserDialog letterChooserDialog =
                        new LetterChooserDialog(activity, letterToPosition,
                                (position -> ((LinearLayoutManager) caller.getLayoutManager()).scrollToPositionWithOffset(position, 0))
                        );
                letterChooserDialog.show();
                activity.autoDismiss(letterChooserDialog);
            });
        }

        public void update(int position) {
            tv_letter.setText(((AppStickyHeader) dataList.get(position)).name);
        }
    }

    public class AppViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        final ImageView app_icon;
        final TextView app_name;
        final ImageView pin;
        final ViewGroup container;
        boolean clicked;
        boolean pinned;

        AppViewHolder(final View itemView) {
            super(itemView);
            container = (ViewGroup) itemView;
            this.app_icon = container.findViewById(R.id.app_icon);
            this.app_name = container.findViewById(R.id.app_name);
            this.pin = container.findViewById(R.id.pin);

            container.setOnClickListener(this);
        }

        public void update(final int index) {
            final App app = (App) AppsRecyclerViewAdapter.this.dataList.get(index);
            this.app_name.setText(app.getLabel());
            AppsDatabaseHelper.loadPic(app, app_icon);

            if (this.pinned) {
                if (!app.isPinned()) {
                    setPinned(false);
                }
            } else {
                if (app.isPinned()) {
                    setPinned(true);
                }
            }
            if (clicked) {
                if (getAdapterPosition() != AppsRecyclerViewAdapter.this.index) {
                    setClicked(false);
                } else {
                    lastView = this;
                }
            } else {
                if (getAdapterPosition() == AppsRecyclerViewAdapter.this.index) {
                    setClicked(true);
                    lastView = this;
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (lastView != null)
                lastView.setClicked(false);
            AppsRecyclerViewAdapter.this.index = getAdapterPosition();
            lastView = this;
            setClicked(true);

            if (changeAppListener != null) {
                changeAppListener.changeApp(AppsRecyclerViewAdapter.this.index);
            }
        }

        public void setClicked(boolean clicked) {
            this.clicked = clicked;
            if (clicked) {
                container.setElevation(elevation);
                container.setBackground(selectedDrawable);
                app_name.setTextColor(textColorOnSelected);
            } else {
                container.setElevation(0);
                container.setBackground(null);
                app_name.setTextColor(textColorOnBackground);

            }
        }

        void setPinned(boolean pinned) {
            this.pinned = pinned;
            if (pinned) {
                this.pin.setVisibility(View.VISIBLE);
            } else {
                this.pin.setVisibility(View.GONE);

            }
        }
    }
}