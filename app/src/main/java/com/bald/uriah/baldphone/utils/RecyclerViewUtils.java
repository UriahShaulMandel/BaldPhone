package com.bald.uriah.baldphone.utils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewUtils {
    public static class BaldGridItemDecoration extends RecyclerView.ItemDecoration {
        private final int sizeGridSpacingPx;
        private final int gridSize;
        private final int paddingInPixels;
        private final Drawable divider;
        private boolean needLeftSpacing = false;

        public BaldGridItemDecoration(int gridSpacingPx, int gridSize, Drawable divider, int paddingInPixels) {
            sizeGridSpacingPx = gridSpacingPx;
            this.gridSize = gridSize;
            this.divider = divider;
            this.paddingInPixels = paddingInPixels;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int frameWidth = (int) ((parent.getWidth() - (float) sizeGridSpacingPx * (gridSize - 1)) / gridSize);
            int padding = parent.getWidth() / gridSize - frameWidth;
            int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
            if (itemPosition < gridSize) {
                outRect.top = 0;
            } else {
                outRect.top = sizeGridSpacingPx;
            }
            if (itemPosition % gridSize == 0) {
                outRect.left = 0;
                outRect.right = padding;
                needLeftSpacing = true;
            } else if ((itemPosition + 1) % gridSize == 0) {
                needLeftSpacing = false;
                outRect.right = 0;
                outRect.left = padding;
            } else if (needLeftSpacing) {
                needLeftSpacing = false;
                outRect.left = sizeGridSpacingPx - padding;
                if ((itemPosition + 2) % gridSize == 0) {
                    outRect.right = sizeGridSpacingPx - padding;
                } else {
                    outRect.right = sizeGridSpacingPx / 2;
                }
            } else if ((itemPosition + 2) % gridSize == 0) {
                outRect.left = sizeGridSpacingPx / 2;
                outRect.right = sizeGridSpacingPx - padding;
            } else {
                outRect.left = sizeGridSpacingPx / 2;
                outRect.right = sizeGridSpacingPx / 2;
            }
            outRect.bottom = 0;
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int left = child.getLeft() + paddingInPixels;
                int right = child.getRight() - paddingInPixels;
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + sizeGridSpacingPx;

                divider.setBounds(left, top, right, bottom);
                divider.draw(c);

                if (i % gridSize != gridSize - 1) {
                    int left2 = child.getRight();
                    int right2 = left2 + sizeGridSpacingPx;
                    int top2 = child.getTop() + paddingInPixels;
                    int bottom2 = child.getBottom() - paddingInPixels;
                    divider.setBounds(left2, top2, right2, bottom2);
                    divider.draw(c);
                }
            }
        }
    }
}
