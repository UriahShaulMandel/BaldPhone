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

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.AppsRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.apps.App;
import com.bald.uriah.baldphone.databases.apps.AppsDatabase;
import com.bald.uriah.baldphone.databases.apps.AppsDatabaseHelper;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.DropDownRecyclerViewAdapter;
import com.bald.uriah.baldphone.utils.S;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Objects;

import static com.bald.uriah.baldphone.adapters.AppsRecyclerViewAdapter.TYPE_HEADER;

public class AppsActivity extends com.bald.uriah.baldphone.activities.BaldActivity {
    private static final String TAG = AppsActivity.class.getSimpleName();
    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String MODE_CHOOSE_ONE = "MODE_CHOOSE_ONE ";
    public static final int UNINSTALL_REQUEST_CODE = 52;
    private static final String SELECTED_APP_INDEX = "SELECTED_APP_INDEX";

    //"finals"
    private AppsDatabase appsDatabase;
    private int numberOfAppsInARow;

    private RecyclerView recyclerView;

    private AppsRecyclerViewAdapter appsRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        AppsDatabaseHelper.updateDB(this);

        appsDatabase = AppsDatabase.getInstance(AppsActivity.this);
        final List<App> appList = appsDatabase.appsDatabaseDao().getAllOrderedByABC();
        recyclerView = findViewById(R.id.rc_apps);
        final boolean modeChoose = MODE_CHOOSE_ONE.equals(getIntent().getStringExtra(EXTRA_MODE));
        appsRecyclerViewAdapter = new AppsRecyclerViewAdapter(appList, this, modeChoose ? this::appChosen : this::showDropDown, recyclerView);

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
        recyclerView.setAdapter(appsRecyclerViewAdapter);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
            recyclerView.getLayoutManager().scrollToPosition(index);
            recyclerView.post(() -> showDropDown(index));
        }
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

    private void showDropDown(final int index) {
        appsRecyclerViewAdapter.index = index;
        final App app = (App) appsRecyclerViewAdapter.dataList.get(index);
        final View view = Objects.requireNonNull(recyclerView.getLayoutManager()).findViewByPosition(index);
        if (view == null)
            return;
        S.showDropDownPopup(this, recyclerView.getWidth(), new DropDownRecyclerViewAdapter.DropDownListener() {
            @Override
            public void onUpdate(DropDownRecyclerViewAdapter.ViewHolder viewHolder, int position, PopupWindow popupWindow) {
                switch (position) {
                    case 0:
                        viewHolder.pic.setImageResource(R.drawable.delete_on_button);
                        viewHolder.text.setText(R.string.uninstall);
                        viewHolder.itemView.setOnClickListener(v1 -> {
                            BDB.from(AppsActivity.this)
                                    .setTitle(String.format("%s %s", getText(R.string.uninstall), app.getLabel()))
                                    .setSubText(String.format(getString(R.string.uninstall_subtext), app.getLabel(), app.getLabel()))
                                    .addFlag(BDialog.FLAG_YES | BDialog.FLAG_CANCEL)
                                    .setPositiveButtonListener(params -> {
                                        uninstallApp(app);
                                        return true;
                                    })
                                    .show();
                            popupWindow.dismiss();
                        });
                        break;
                    case 1:
                        if (S.isValidContextForGlide(viewHolder.pic.getContext()))
                            Glide.with(viewHolder.pic).load(app.getIcon()).into(viewHolder.pic);
                        viewHolder.text.setText(R.string.open);
                        viewHolder.itemView.setOnClickListener(v1 -> {
                            final ComponentName componentName = ComponentName.unflattenFromString(app.getFlattenComponentName());
                            S.startComponentName(AppsActivity.this, componentName);
                            popupWindow.dismiss();
                        });
                        break;
                    case 2:
                        viewHolder.pic.setImageResource(app.isPinned() ? R.drawable.remove_on_button : R.drawable.add_on_button);
                        viewHolder.text.setText(app.isPinned() ? R.string.remove_shortcut : R.string.add_shortcut);
                        viewHolder.itemView.setOnClickListener(v1 -> {
                            if (app.isPinned()) {
                                appsDatabase.appsDatabaseDao().update(app.getId(), false);
                                app.setPinned(false);
                                appsRecyclerViewAdapter.notifyItemChanged(appsRecyclerViewAdapter.index);
                            } else {
                                appsDatabase.appsDatabaseDao().update(app.getId(), true);
                                app.setPinned(true);
                                appsRecyclerViewAdapter.notifyItemChanged(appsRecyclerViewAdapter.index);
                            }
                            popupWindow.dismiss();
                            showDropDown(index);
                        });
                        break;
                }
            }

            @Override
            public int size() {
                return 3;
            }

            @Override
            public void onDismiss() {
                if (appsRecyclerViewAdapter.lastView != null)
                    appsRecyclerViewAdapter.lastView.setClicked(false);
                appsRecyclerViewAdapter.lastView = null;
                appsRecyclerViewAdapter.index = -1;

            }
        }, view);

        if (index + numberOfAppsInARow >= appsRecyclerViewAdapter.dataList.size())
            recyclerView.scrollToPosition(index);

    }

    private void appChosen(int index) {
        if (index != -1) {
            final App app = (App) appsRecyclerViewAdapter.dataList.get(index);
            setResult(RESULT_OK, new Intent().setComponent(ComponentName.unflattenFromString(app.getFlattenComponentName())));
            finish();
        }
    }

    public interface ChangeAppListener {
        void changeApp(int index);
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}