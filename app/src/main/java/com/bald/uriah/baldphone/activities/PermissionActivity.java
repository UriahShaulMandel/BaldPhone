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

package com.bald.uriah.baldphone.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.homescreen.HomeScreenActivity;
import com.bald.uriah.baldphone.core.BDB;
import com.bald.uriah.baldphone.core.BDialog;
import com.bald.uriah.baldphone.core.BPrefs;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_CALL_LOG;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES;

public class PermissionActivity extends BaldActivity {
    public static final String
            EXTRA_REQUIRED_PERMISSIONS = "EXTRA_REQUIRED_PERMISSIONS",
            EXTRA_INTENT = "EXTRA_INTENT";
    public static final int[] REQUEST_CODES = {789, 788, 787, 786};

    private List<PermissionItem> permissionItemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private int requiredPermissions;
    private Intent ancestorCallingIntent;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        final Intent callingIntent = getIntent();
        requiredPermissions = callingIntent.getIntExtra(EXTRA_REQUIRED_PERMISSIONS, -1);
        ancestorCallingIntent = callingIntent.getParcelableExtra(EXTRA_INTENT);

        recyclerView = findViewById(R.id.child);
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.ll_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(new PermissionRecyclerViewAdapter());
        BaldTitleBar baldTitleBar = findViewById(R.id.bald_title_bar);
        baldTitleBar.getBt_back().setOnClickListener(v -> calmyBDB());

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPermissions();
    }

    private void calmyBDB() {
        if (isTaskRoot()) {
            BDB.from(this)
                    .setTitle(R.string.permissions_part)
                    .setSubText(R.string.permissions_calm)
                    .addFlag(BDialog.FLAG_OK | BDialog.FLAG_NO)
                    .setPositiveButtonListener(params -> true)
                    .setNegativeButtonListener(params -> {
                        FakeLauncherActivity.resetPreferredLauncherAndOpenChooser(this);
                        return true;
                    })
                    .show();
        } else
            finish();
    }

    private void refreshPermissions() {
        obtainPermissionList();
        if (permissionItemList.isEmpty()) {
            startActivity(ancestorCallingIntent == null ? new Intent(this, HomeScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) : ancestorCallingIntent);
            finish();
            return;
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void obtainPermissionList() {
        permissionItemList.clear();
        if (requiredPermissions == PERMISSION_NONE)
            return;

        if ((requiredPermissions & PERMISSION_DEFAULT_PHONE_HANDLER) != 0) {
            if (!defaultDialerGranted(this)) {
                permissionItemList.add(
                        new PermissionItem(v -> startActivityForResult(new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName()), REQUEST_CODES[1]), getString(R.string.set_default_dialer), getString(R.string.set_default_dialer_subtext)));
            }
        }
        if ((requiredPermissions & PERMISSION_WRITE_SETTINGS) != 0) {
            if (!writeSettingsGranted(this)) {
                permissionItemList.add(
                        new PermissionItem(v -> PermissionActivity.this.startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                .setData(Uri.parse("package:" + PermissionActivity.this.getPackageName())), 798),
                                getString(R.string.settings_permission),
                                getString(R.string.change_system_settings_subtext)));
            } else if ((requiredPermissions & PERMISSION_NOTIFICATION_LISTENER) == PERMISSION_NOTIFICATION_LISTENER) {
                if (!notificationListenerGranted(this)) {
                    permissionItemList.add(new PermissionItem((v) -> startActivityForResult(new Intent(
                            "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_CODES[0]), getString(R.string.enable_notification_access), getString(R.string.enable_notification_access_subtext)));
                }
            }
        }
        if (defaultDialerGranted(this)) {
            if ((requiredPermissions & PERMISSION_READ_CONTACTS) != 0) {
                if (ActivityCompat.checkSelfPermission(this, READ_CONTACTS) != PERMISSION_GRANTED)
                    permissionItemList.add(
                            new SimplePermissionItem(READ_CONTACTS, getString(R.string.read_contacts), getString(R.string.contacts_read_subtext)));
            }
            if ((requiredPermissions & PERMISSION_WRITE_CONTACTS) != 0) {
                if (ActivityCompat.checkSelfPermission(this, WRITE_CONTACTS) != PERMISSION_GRANTED)
                    permissionItemList.add(
                            new SimplePermissionItem(WRITE_CONTACTS, getString(R.string.write_contacts), getString(R.string.contacts_subtext)));
            }
            if ((requiredPermissions & PERMISSION_CALL_PHONE) != 0) {
                if (ActivityCompat.checkSelfPermission(this, CALL_PHONE) != PERMISSION_GRANTED)
                    permissionItemList.add(
                            new SimplePermissionItem(CALL_PHONE, getString(R.string.calling), getString(R.string.call_subtext)));
            }
            if ((requiredPermissions & PERMISSION_WRITE_CALL_LOG) != 0 || (requiredPermissions & PERMISSION_READ_CALL_LOG) != 0) {
                if (!BuildConfig.FLAVOR.equals("gPlay"))
                    if (ActivityCompat.checkSelfPermission(this, WRITE_CALL_LOG) != PERMISSION_GRANTED)
                        permissionItemList.add(
                                new SimplePermissionItem(WRITE_CALL_LOG, getString(R.string.read_call_log), getString(R.string.read_call_log_subtext)));
                    else if (ActivityCompat.checkSelfPermission(this, READ_CALL_LOG) != PERMISSION_GRANTED)
                        permissionItemList.add(
                                new SimplePermissionItem(READ_CALL_LOG, getString(R.string.read_call_log), getString(R.string.read_call_log_subtext)));
            }
            if ((requiredPermissions & PERMISSION_READ_PHONE_STATE) != 0) {
                if (BPrefs.get(this).getBoolean(BPrefs.DUAL_SIM_KEY, BPrefs.DUAL_SIM_DEFAULT_VALUE))
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
                        if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PERMISSION_GRANTED)
                            permissionItemList.add(
                                    new SimplePermissionItem(READ_PHONE_STATE, getString(R.string.read_phone_state), getString(R.string.read_phone_state_subtext)));
            }

        }
        if ((requiredPermissions & PERMISSION_CAMERA) != 0) {
            if (ActivityCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED)
                permissionItemList.add(
                        new SimplePermissionItem(CAMERA, getString(R.string.camera), getString(R.string.camera_subtext)));
        }
        if ((requiredPermissions & PERMISSION_REQUEST_INSTALL_PACKAGES) != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && BuildConfig.FLAVOR.equals("baldUpdates")) {
                if (!getPackageManager().canRequestPackageInstalls())
                    permissionItemList.add(
                            new PermissionItem(v -> startActivityForResult(new Intent(
                                    ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), REQUEST_CODES[2]), getString(R.string.install_updates), getString(R.string.install_updates_subtext)));
            }
        }
        if ((requiredPermissions & PERMISSION_WRITE_EXTERNAL_STORAGE) != 0) {
            if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
                permissionItemList.add(
                        new SimplePermissionItem(WRITE_EXTERNAL_STORAGE, getString(R.string.write_external_storage), getString(R.string.external_storage_subtext)));
        }
        if ((requiredPermissions & PERMISSION_SYSTEM_ALERT_WINDOW) != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!Settings.canDrawOverlays(this)) {
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    permissionItemList.add(
                            new PermissionItem(v -> startActivityForResult(intent, REQUEST_CODES[3]), getString(R.string.appear_on_top), getString(R.string.appear_on_top_subtext)));
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (S.intArrayContains(REQUEST_CODES, requestCode))
            refreshPermissions();
    }

    @Override
    public void onBackPressed() {
    }

    private class PermissionRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<PermissionRecyclerViewAdapter.ViewHolder> {
        final LayoutInflater layoutInflater;

        PermissionRecyclerViewAdapter() {
            layoutInflater = PermissionActivity.this.getLayoutInflater();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.permission_item, parent, false));
        }

        @Override
        public int getItemCount() {
            return permissionItemList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            final PermissionItem permissionItem = permissionItemList.get(position);
            holder.title.setText(permissionItem.title);
            holder.explanation.setText(permissionItem.explanation);
            holder.allow.setOnClickListener(permissionItem.onClickListener);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView title, explanation, allow;

            public ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                explanation = itemView.findViewById(R.id.explanation);
                allow = itemView.findViewById(R.id.allow);
            }
        }
    }

    private class PermissionItem {
        final View.OnClickListener onClickListener;
        final String title, explanation;

        PermissionItem(View.OnClickListener onClickListener, String title, String explanation) {
            this.onClickListener = onClickListener;
            this.title = title;
            this.explanation = explanation;
        }
    }

    private class SimplePermissionItem extends PermissionItem {
        SimplePermissionItem(String permission, String title, String explanation) {
            super((v) -> ActivityCompat.requestPermissions(PermissionActivity.this, new String[]{permission}, 789),
                    title,
                    explanation);
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
