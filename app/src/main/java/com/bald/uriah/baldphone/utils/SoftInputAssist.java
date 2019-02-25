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

package com.bald.uriah.baldphone.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Taken from this thread:
 * https://stackoverflow.com/questions/7417123/android-how-to-adjust-layout-in-full-screen-mode-when-softkeyboard-is-visible
 */
public class SoftInputAssist {
    private View rootView;
    private ViewGroup contentContainer;
    private ViewTreeObserver viewTreeObserver;
    private int usableHeightPrevious = 0;
    private boolean dead;

    private final Rect contentAreaOfWindowBounds = new Rect();
    private final FrameLayout.LayoutParams rootViewLayout;

    public SoftInputAssist(Activity activity) {
        contentContainer = activity.findViewById(android.R.id.content);
        rootView = contentContainer.getChildAt(0);
        rootViewLayout = (FrameLayout.LayoutParams) rootView.getLayoutParams();
    }

    public void onPause() {
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
        }
    }

    public void onResume() {
        if (viewTreeObserver == null || !viewTreeObserver.isAlive()) {
            viewTreeObserver = rootView.getViewTreeObserver();
        }

        viewTreeObserver.addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
    }

    public void onDestroy() {
        rootView = null;
        contentContainer = null;
        viewTreeObserver = null;
        dead = true;
    }

    private void possiblyResizeChildOfContent() {
        if (dead)
            return;
        contentContainer.getWindowVisibleDisplayFrame(contentAreaOfWindowBounds);
        int usableHeightNow = contentAreaOfWindowBounds.height();

        if (usableHeightNow != usableHeightPrevious) {
            rootViewLayout.height = usableHeightNow;
            rootView.layout(contentAreaOfWindowBounds.left, contentAreaOfWindowBounds.top, contentAreaOfWindowBounds.right, contentAreaOfWindowBounds.bottom);
            rootView.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }
}