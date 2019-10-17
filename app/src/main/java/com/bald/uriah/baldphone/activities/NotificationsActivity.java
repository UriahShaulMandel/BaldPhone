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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.NotificationRecyclerViewAdapter;
import com.bald.uriah.baldphone.services.NotificationListenerService;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;

import static com.bald.uriah.baldphone.services.NotificationListenerService.ACTION_REGISTER_ACTIVITY;
import static com.bald.uriah.baldphone.services.NotificationListenerService.ACTIVITY_NONE;
import static com.bald.uriah.baldphone.services.NotificationListenerService.KEY_EXTRA_ACTIVITY;
import static com.bald.uriah.baldphone.services.NotificationListenerService.KEY_EXTRA_NOTIFICATIONS;
import static com.bald.uriah.baldphone.services.NotificationListenerService.NOTIFICATIONS_ACTIVITY;

public class NotificationsActivity extends BaldActivity {
    private static final String TAG = NotificationsActivity.class.getSimpleName();
    public Bundle[] activeNotifications;
    public RecyclerView recyclerView;
    private NotificationRecyclerViewAdapter notificationRecyclerViewAdapter;
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(NotificationListenerService.NOTIFICATIONS_ACTIVITY_BROADCAST))
                throw new AssertionError("!intent.getAction().equals(NotificationListenerService.NOTIFICATIONS_ACTIVITY_BROADCAST)");
            activeNotifications =
                    (Bundle[]) intent.getParcelableArrayExtra(KEY_EXTRA_NOTIFICATIONS);
            if (notificationRecyclerViewAdapter == null) {
                notificationRecyclerViewAdapter =
                        new NotificationRecyclerViewAdapter(NotificationsActivity.this, activeNotifications);
                recyclerView.setAdapter(notificationRecyclerViewAdapter);
            } else {
                notificationRecyclerViewAdapter.changeNotifications(activeNotifications);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!notificationListenerGranted(this))
            return;

        setContentView(R.layout.activity_notifications);
        findViewById(R.id.clear_all_notifications).setOnClickListener(v -> {
            if (notificationRecyclerViewAdapter != null)
                notificationRecyclerViewAdapter.clearAll();
            finish();
        });
        recyclerView = findViewById(R.id.recycler_view);
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.ll_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemViewCacheSize(10);

        if (!Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners")
                .contains(getApplicationContext().getPackageName())) {
            BDB.from(this)
                    .setTitle(R.string.enable_notification_access)
                    .setSubText(R.string.enable_notification_access_subtext)
                    .addFlag(BDialog.FLAG_OK | BDialog.FLAG_CANCEL)
                    .setPositiveButtonListener(params -> {
                        getApplicationContext().startActivity(new Intent(
                                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        return true;
                    })
                    .setNegativeButtonListener(params -> true)
                    .show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                new IntentFilter(NotificationListenerService.NOTIFICATIONS_ACTIVITY_BROADCAST));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_REGISTER_ACTIVITY).putExtra(KEY_EXTRA_ACTIVITY, NOTIFICATIONS_ACTIVITY));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_REGISTER_ACTIVITY).putExtra(KEY_EXTRA_ACTIVITY, ACTIVITY_NONE));

        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_up);
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NOTIFICATION_LISTENER;
    }
}
