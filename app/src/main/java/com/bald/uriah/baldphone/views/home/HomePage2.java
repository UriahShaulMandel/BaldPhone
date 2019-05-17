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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.HomeScreenActivity;
import com.bald.uriah.baldphone.activities.SettingsActivity;
import com.bald.uriah.baldphone.activities.VideoTutorialsActivity;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.DropDownRecyclerViewAdapter;
import com.bald.uriah.baldphone.utils.S;
import com.bumptech.glide.Glide;

import java.util.List;

public class HomePage2 extends HomeView {
    public static final String TAG = HomePage2.class.getSimpleName();
    private View view;
    private ImageView iv_internet, iv_maps;
    private TextView tv_internet, tv_maps;
    private View bt_settings, bt_internet, bt_maps, bt_help;
    private PackageManager packageManager;

    public HomePage2(@NonNull HomeScreenActivity homeScreen) {
        super(homeScreen);
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
                    S.showDropDownPopup(
                            homeScreen,
                            getWidth(),
                            new DropDownRecyclerViewAdapter.DropDownListener() {
                                @Override
                                public void onUpdate(DropDownRecyclerViewAdapter.ViewHolder viewHolder, int position, PopupWindow popupWindow) {
                                    final ResolveInfo resolveInfo = browsers.get(position);
                                    Glide.with(viewHolder.pic)
                                            .load(resolveInfo.loadIcon(packageManager))
                                            .into(viewHolder.pic);
                                    viewHolder.text.setText(resolveInfo.loadLabel(packageManager));
                                    viewHolder.itemView.setOnClickListener(v1 -> {
                                        homeScreen.startActivity(packageManager.getLaunchIntentForPackage(resolveInfo.activityInfo.applicationInfo.packageName));
                                        popupWindow.dismiss();
                                    });
                                }

                                @Override public int size() {
                                    return browsers.size();
                                }

                            }, bt);
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

}