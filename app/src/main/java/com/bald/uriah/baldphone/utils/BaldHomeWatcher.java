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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * with help from {@link "https://stackoverflow.com/questions/8881951/detect-home-button-press-in-android"}
 * but suited for BaldPhone...
 */
public class BaldHomeWatcher {
    private static final String TAG = BaldHomeWatcher.class.getSimpleName();
    private final Context context;
    private final IntentFilter filter;
    private final OnHomePressedListener listener;
    private final HomeClicksReceiver receiver;

    public BaldHomeWatcher(Context context, OnHomePressedListener onHomePressedListener) {
        this.context = context;
        this.listener = onHomePressedListener;
        filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        receiver = new HomeClicksReceiver();
    }

    public void startWatch() {
        if (receiver != null)
            context.registerReceiver(receiver, filter);
    }

    public void stopWatch() {
        if (receiver != null)
            context.unregisterReceiver(receiver);
    }

    @FunctionalInterface
    public interface OnHomePressedListener {
        void onHomePressed();
    }

    class HomeClicksReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    Log.d(TAG, "action: " + action + " ,reason: " + reason);
                    if (listener != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            listener.onHomePressed();
                        }
                    }
                }
            }
        }
    }
}