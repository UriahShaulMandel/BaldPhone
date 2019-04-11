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

package com.bald.uriah.baldphone.activities;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.AppsRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.apps.App;
import com.bald.uriah.baldphone.databases.apps.AppsDatabase;
import com.bald.uriah.baldphone.databases.apps.AppsDatabaseHelper;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.S;

import java.util.List;

import static com.bald.uriah.baldphone.adapters.AppsRecyclerViewAdapter.TYPE_HEADER;

public class AppsActivity extends com.bald.uriah.baldphone.activities.BaldActivity {
    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String MODE_CHOOSE_ONE = "MODE_CHOOSE_ONE ";

    public static final int UNINSTALL_REQUEST_CODE = 52;
    private static final String TAG = AppsActivity.class.getSimpleName();
    private static final String SELECTED_APP_INDEX = "SELECTED_APP_INDEX";
    private static final int TIME_FOR_EFFECT = 300;
    private int lastIndex = -1;

    //"finals"
    private AppsDatabase appsDatabase;
    private Drawable drawablePin, drawableRemPin;
    private int numberOfAppsInARow;
    private int barHeight;

    //views
    private View bar, pin, open, uninstall;
    private TextView tv_add_or_rem_shortcut;
    private ImageView iv_pin;
    private RecyclerView recyclerView;

    private AppsRecyclerViewAdapter appsRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        AppsDatabaseHelper.updateDB(this);

        drawablePin = ContextCompat.getDrawable(this, R.drawable.add_on_button);
        drawableRemPin = ContextCompat.getDrawable(this, R.drawable.remove_on_button);
        barHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120f, getResources().getDisplayMetrics());

        appsDatabase = AppsDatabase.getInstance(AppsActivity.this);
        final List<App> appList = appsDatabase.appsDatabaseDao().getAllOrderedByABC();
        attachXml();

        final boolean modeChoose = MODE_CHOOSE_ONE.equals(getIntent().getStringExtra(EXTRA_MODE));
        appsRecyclerViewAdapter = new AppsRecyclerViewAdapter(appList, this, modeChoose ? this::appChosen : this::changeBar, recyclerView);

        final WindowManager windowManager = getWindowManager();
        final Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        final boolean screenOrientation = (point.x / point.y) != 0;
        numberOfAppsInARow = screenOrientation ? 6 : 3;
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfAppsInARow);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (appsRecyclerViewAdapter.getItemViewType(position)) {
                    case TYPE_HEADER:
                        return numberOfAppsInARow;
                    case AppsRecyclerViewAdapter.TYPE_ITEM:
                        return 1;
                    default:
                        return 1;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);

        open.setOnClickListener(v -> {
            if (appsRecyclerViewAdapter.index != -1) {
                final ComponentName componentName = ComponentName.unflattenFromString(((App) appsRecyclerViewAdapter.dataList.get(appsRecyclerViewAdapter.index)).getFlattenComponentName());
                S.startComponentName(v.getContext(), componentName);
            }
        });
        pin.setOnClickListener(v -> {
            if (appsRecyclerViewAdapter.index != -1) {

                final App app = (App) appsRecyclerViewAdapter.dataList.get(appsRecyclerViewAdapter.index);
                if (app.isPinned()) {
                    appsDatabase.appsDatabaseDao().update(app.getId(), false);
                    app.setPinned(false);
                    appsRecyclerViewAdapter.notifyItemChanged(appsRecyclerViewAdapter.index);
                    iv_pin.setImageDrawable(drawablePin);
                    tv_add_or_rem_shortcut.setText(R.string.add_shortcut);
                } else {
                    appsDatabase.appsDatabaseDao().update(app.getId(), true);
                    app.setPinned(true);
                    appsRecyclerViewAdapter.notifyItemChanged(appsRecyclerViewAdapter.index);
                    iv_pin.setImageDrawable(drawableRemPin);
                    tv_add_or_rem_shortcut.setText(R.string.remove_shortcut);

                }
            }
        });
        uninstall.setOnClickListener(v -> {
            final App app = (App) appsRecyclerViewAdapter.dataList.get(appsRecyclerViewAdapter.index);
            BDB.from(this)
                    .setTitle(getText(R.string.uninstall) + app.getLabel())
                    .setSubText(String.format(getString(R.string.uninstall_subtext), app.getLabel(), app.getLabel()))
                    .setDialogState(BDialog.DialogState.YES_CANCEL)
                    .setCancelable(true)
                    .setPositiveButtonListener(params -> {
                        uninstallApp(app);
                        return true;
                    })
                    .show();
        });
        recyclerView.setAdapter(appsRecyclerViewAdapter);
    }

    private void attachXml() {
        recyclerView = findViewById(R.id.rc_apps);
        bar = findViewById(R.id.bar);
        pin = bar.findViewById(R.id.pin);
        open = bar.findViewById(R.id.open);
        uninstall = bar.findViewById(R.id.app_uninstall);
        iv_pin = findViewById(R.id.iv_pin);
        tv_add_or_rem_shortcut = findViewById(R.id.tv_add_or_rem_shortcut);
    }

    private void uninstallApp(App app) {
        String app_pkg_name = ComponentName.unflattenFromString(app.getFlattenComponentName()).getPackageName();
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + app_pkg_name));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        }
    }

    private void changeBar(int index) {
        if (lastIndex == index)
            return;
        lastIndex = index;
        if (index == -1) {
            animateBarView(false);
        } else {
            final App app = (App) appsRecyclerViewAdapter.dataList.get(index);
            bar.setVisibility(View.VISIBLE);
            animateBarView(true);
            if (index + numberOfAppsInARow >= appsRecyclerViewAdapter.dataList.size())
                recyclerView.scrollToPosition(index);

            if (app.isPinned()) {
                tv_add_or_rem_shortcut.setText(R.string.remove_shortcut);
                iv_pin.setImageDrawable(drawableRemPin);
            } else {
                iv_pin.setImageDrawable(drawablePin);
                tv_add_or_rem_shortcut.setText(R.string.add_shortcut);
            }
        }
    }


    private void appChosen(int index) {
        if (lastIndex == index)
            return;
        lastIndex = index;
        if (index == -1) {
            animateBarView(false);
        } else {
            final App app = (App) appsRecyclerViewAdapter.dataList.get(index);
            setResult(RESULT_OK, new Intent().setComponent(ComponentName.unflattenFromString(app.getFlattenComponentName())));
            finish();
        }
    }

    public void animateBarView(boolean up) {
        final int desiredInt = up ? barHeight : 0;
        final ValueAnimator animator = ValueAnimator.ofInt(((ConstraintLayout.LayoutParams) bar.getLayoutParams()).height, desiredInt);
        animator.addUpdateListener(valueAnimator -> {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) bar.getLayoutParams();
                    params.height = (Integer) valueAnimator.getAnimatedValue();
                    bar.setLayoutParams(params);
                }
        );
        animator.setDuration(TIME_FOR_EFFECT);
        animator.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_APP_INDEX, ((AppsRecyclerViewAdapter) recyclerView.getAdapter()).index);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int index = savedInstanceState.getInt(SELECTED_APP_INDEX);
        final AppsRecyclerViewAdapter adapter = ((AppsRecyclerViewAdapter) recyclerView.getAdapter());
        if (index < adapter.dataList.size() && index > 0 && adapter.dataList.get(index).type() != TYPE_HEADER) {
            adapter.index = index;
            changeBar(index);
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }

    public interface ChangeAppListener {
        void changeApp(int index);
    }
}