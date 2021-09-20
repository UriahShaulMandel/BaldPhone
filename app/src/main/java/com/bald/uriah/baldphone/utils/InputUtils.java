package com.bald.uriah.baldphone.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class InputUtils {
    /**
     * Taken from this thread:
     * https://stackoverflow.com/questions/7417123/android-how-to-adjust-layout-in-full-screen-mode-when-softkeyboard-is-visible
     */
    public static class SoftInputAssist {
        private final Rect contentAreaOfWindowBounds = new Rect();
        private final FrameLayout.LayoutParams rootViewLayout;
        private View rootView;
        private ViewGroup contentContainer;
        private ViewTreeObserver viewTreeObserver;
        private int usableHeightPrevious = 0;
        private boolean dead;

        public SoftInputAssist(Activity activity) {
            contentContainer = activity.findViewById(android.R.id.content);
            rootView = contentContainer.getChildAt(0);
            rootViewLayout = (FrameLayout.LayoutParams) rootView.getLayoutParams();
        }

        public void onResume() {
            if (viewTreeObserver == null || !viewTreeObserver.isAlive()) {
                viewTreeObserver = rootView.getViewTreeObserver();
            }

            viewTreeObserver.addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
        }

        public void onPause() {
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
            }
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
}
