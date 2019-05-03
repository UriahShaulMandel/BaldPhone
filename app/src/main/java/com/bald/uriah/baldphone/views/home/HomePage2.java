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

package com.bald.uriah.baldphone.views.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.HomeScreenActivity;
import com.bald.uriah.baldphone.activities.SettingsActivity;
import com.bald.uriah.baldphone.activities.VideoTutorialsActivity;
import com.bald.uriah.baldphone.adapters.IntentAdapter;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.ModularRecyclerView;
import com.bumptech.glide.Glide;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

public class HomePage2 extends HomeView {
    public static final String TAG = HomePage2.class.getSimpleName();
    private static final float DIM_AMOUNT = 0.7f;
    private View view;
    private ImageView iv_internet, iv_maps;
    private TextView tv_internet, tv_maps;
    private View bt_settings, bt_internet, bt_maps, bt_help;
    private PackageManager packageManager;

    public HomePage2(@NonNull HomeScreenActivity homeScreen) {
        super(homeScreen);
    }

    public static void applyDim(@NonNull ViewGroup parent) {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * DIM_AMOUNT));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.fragment_home_page2, container, false);
        packageManager = homeScreen.getPackageManager();
        attachXml();
        genOnLongClickListeners();
        return view;
    }

    private void attachXml() {
        bt_settings = view.findViewById(R.id.bt_settings);
        bt_internet = view.findViewById(R.id.bt_apps);
        bt_maps = view.findViewById(R.id.bt_maps);
        iv_internet = view.findViewById(R.id.iv_internet);
        iv_maps = view.findViewById(R.id.iv_maps);
        tv_internet = view.findViewById(R.id.tv_internet);
        tv_maps = view.findViewById(R.id.tv_maps);
        bt_help = view.findViewById(R.id.bt_help);

    }

    private void genOnLongClickListeners() {
        bt_settings.setOnClickListener(v ->
                homeScreen.startActivity(new Intent(getContext(), SettingsActivity.class)));

        clickListenerForAbstractOpener(Uri.parse("about:blank"), bt_internet, iv_internet, tv_internet);
        clickListenerForAbstractOpener(Uri.parse("geo:0,0"), bt_maps, iv_maps, tv_maps);

        bt_help.setOnClickListener(v ->
                homeScreen.startActivity(new Intent(getContext(), VideoTutorialsActivity.class)));

    }

    private void clickListenerForAbstractOpener(@NonNull final Uri uri, @NonNull final View bt, @NonNull final ImageView iv, @NonNull final TextView tv) {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        @Nullable final ComponentName browserComponentName = browserIntent.resolveActivity(packageManager);
        if (browserComponentName == null) {
            bt.setOnClickListener(this::showErrorMessage);
        } else if (browserComponentName.getClassName().equals("com.android.internal.app.ResolverActivity")) {
            final List<ResolveInfo> browsers = packageManager.queryIntentActivities(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (!browsers.isEmpty()) {
                bt.setOnClickListener(v -> {
                    final RelativeLayout dropDownContainer = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.drop_down_recycler_view, null, false);
                    final RecyclerView recyclerView = dropDownContainer.findViewById(R.id.recycler_view);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }
                    });
                    final PopupWindow popupWindow = new PopupWindow(dropDownContainer, (int) (getWidth() / 1.3), ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    recyclerView.addItemDecoration(
                            new HorizontalDividerItemDecoration.Builder(getContext())
                                    .drawable(R.drawable.settings_divider)
                                    .build()
                    );
                    recyclerView.setAdapter(new DropDownRecyclerViewAdapter(getContext(), browsers,
                            (resolveInfo, context) -> {
                                context.startActivity(packageManager.getLaunchIntentForPackage(resolveInfo.activityInfo.applicationInfo.packageName));
                                popupWindow.dismiss();
                            }));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        popupWindow.setOverlapAnchor(true);

                    final ViewGroup root = (ViewGroup) homeScreen.getWindow().getDecorView().getRootView();
                    popupWindow.setOnDismissListener(() -> clearDim(root));
                    popupWindow.setBackgroundDrawable(getContext().getDrawable(R.drawable.empty));
                    popupWindow.showAsDropDown(bt);
                    homeScreen.autoDismiss(popupWindow);
                    applyDim(root);
                });
            } else {
                bt.setOnClickListener(this::showErrorMessage);
            }
        } else {
            bt.setOnClickListener(v -> homeScreen.startActivity(packageManager.getLaunchIntentForPackage(browserComponentName.getPackageName())));
            try {
                final ActivityInfo activityInfo = packageManager.getActivityInfo(browserComponentName, PackageManager.MATCH_DEFAULT_ONLY);
                final ApplicationInfo applicationInfo = activityInfo.applicationInfo;
                final Drawable drawable = applicationInfo.loadIcon(packageManager);
                iv.setImageDrawable(drawable);
                tv.setText(applicationInfo.loadLabel(packageManager));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, S.str(e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    private void showErrorMessage(View v) {
        BaldToast.from(v.getContext()).setType(BaldToast.TYPE_ERROR).setText(R.string.no_app_was_found).show();
    }

    static class DropDownRecyclerViewAdapter extends RecyclerView.Adapter<DropDownRecyclerViewAdapter.ViewHolder> {
        private final LayoutInflater layoutInflater;
        private final List<ResolveInfo> resolveInfoList;
        private final PackageManager packageManager;
        private final IntentAdapter.ResolveInfoConsumer resolveInfoConsumer;
        private final Context context;

        public DropDownRecyclerViewAdapter(final Context context, final List<ResolveInfo> resolveInfoList, final IntentAdapter.ResolveInfoConsumer resolveInfoConsumer) {
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
                resolveInfoConsumer.consume(resolveInfoList.get(getAdapterPosition()), context);
            }
        }

    }
}