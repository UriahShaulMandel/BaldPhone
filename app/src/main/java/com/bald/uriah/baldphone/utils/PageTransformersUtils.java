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

import androidx.viewpager.widget.ViewPager;

/**
 * right now this feature is still working, but not available in the settings.
 * the reason for it is that i want to deprecate it, but not sure yet.
 */
public class PageTransformersUtils {
    private static final ViewPager.PageTransformer OPTION_C = (page, position) -> page.setRotationY(position * -30);
    private static final ViewPager.PageTransformer OPTION_B = (page, position) -> {
        final float normalizedposition = Math.abs(Math.abs(position) - 1);
        page.setScaleX(normalizedposition / 2 + 0.5f);
        page.setScaleY(normalizedposition / 2 + 0.5f);
    };
    private final static float MIN_SCALE = 0.85f;
    private final static float MIN_ALPHA = 0.5f;
    private static final ViewPager.PageTransformer OPTION_A = (page, position) -> {
        final int pageWidth = page.getWidth();
        final int pageHeight = page.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            final float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            final float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            final float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                page.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                page.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            page.setAlpha(MIN_ALPHA +
                    (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0);
        }
    };

    public static ViewPager.PageTransformer[] pageTransformers = new ViewPager.PageTransformer[]{OPTION_A, OPTION_B, OPTION_C};
}
