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

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bald.uriah.baldphone.R;

public class AnimationsUtils {

    public static void makeBiggerAndSmaller(final Context context, final View view, final Runnable runnable) {
        final Animation enlarge =
                AnimationUtils.loadAnimation(context, R.anim.enlarge);
        final Animation ensmall =
                AnimationUtils.loadAnimation(context, R.anim.ensmall);
        enlarge.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(ensmall);
                if (runnable != null)
                    runnable.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        ensmall.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(enlarge);
                if (runnable != null)
                    runnable.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(enlarge);
    }
}
