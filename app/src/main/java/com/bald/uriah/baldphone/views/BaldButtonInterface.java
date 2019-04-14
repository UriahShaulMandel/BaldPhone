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

package com.bald.uriah.baldphone.views;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.bald.uriah.baldphone.utils.BaldToast;

import java.util.ArrayList;
import java.util.List;

public interface BaldButtonInterface {
    String TAG = BaldButtonInterface.class.getSimpleName();
    int MEDIUM_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout() / 2;
    int MEDIUM_PRESS_DISTANCE = 200;

    void baldPerformClick();

    void vibrate();


    class BaldButtonTouchListener implements View.OnTouchListener {
        private final BaldButtonInterface v;
        private final Handler longPressHandler;
        private final List<View.OnTouchListener> otherListeners = new ArrayList<>();
        private boolean isLongPressHandlerActivated = false;
        private boolean isActionMoveEventStored = false;
        private float lastActionMoveEventBeforeUpX;
        private float lastActionMoveEventBeforeUpY;
        private Runnable longPressedRunnable = new Runnable() {
            public void run() {
                v.baldPerformClick();
                v.vibrate();
                isLongPressHandlerActivated = true;
            }
        };

        public BaldButtonTouchListener(BaldButtonInterface v) {
            this.v = v;
            longPressHandler = new Handler();
        }

        public void addListener(View.OnTouchListener listener) {
            otherListeners.add(listener);
        }

        boolean consumeOthers(View v, MotionEvent event) {
            boolean ret = false;
            for (View.OnTouchListener onTouchListener : otherListeners)
                ret |= onTouchListener.onTouch(v, event);

            return ret;

        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(final View v, final MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                longPressHandler.postDelayed(longPressedRunnable, MEDIUM_PRESS_TIMEOUT);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                if (!isActionMoveEventStored) {
                    isActionMoveEventStored = true;
                    lastActionMoveEventBeforeUpX = event.getX();
                    lastActionMoveEventBeforeUpY = event.getY();
                } else {
                    final float currentX = event.getX();
                    final float currentY = event.getY();
                    final float firstX = lastActionMoveEventBeforeUpX;
                    final float firstY = lastActionMoveEventBeforeUpY;
                    final double distance = Math.sqrt(
                            (currentY - firstY) * (currentY - firstY) + ((currentX - firstX) * (currentX - firstX)));
                    if (distance > MEDIUM_PRESS_DISTANCE) {
                        longPressHandler.removeCallbacks(longPressedRunnable);
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                isActionMoveEventStored = false;
                longPressHandler.removeCallbacks(longPressedRunnable);
                if (isLongPressHandlerActivated) {
                    Log.d(TAG, "Long Press detected; halting propagation of motion event");
                    isLongPressHandlerActivated = false;
                    return consumeOthers(v, event);
                } else {
                    BaldToast.longer(v.getContext());
                    return consumeOthers(v, event);
                }

            }
            return consumeOthers(v, event);
        }
    }
}
