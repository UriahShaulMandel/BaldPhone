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

package com.bald.uriah.baldphone.fragments_and_dialogs.tutorial_fragments;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bald.uriah.baldphone.R;

public class TutorialFragment1 extends TutorialFragment {
    private TextView hello, and_welcome_to_baldphone;

    protected void attachXml() {
        hello = root.findViewById(R.id.hello);
        and_welcome_to_baldphone = root.findViewById(R.id.and_welcome_to_baldphone);
    }

    private void firstAnimation(final View view, final Runnable afterAnimation, final long msDuration) {
        final LinearLayout.LayoutParams layoutParamsBefore = (LinearLayout.LayoutParams) view.getLayoutParams();
        final int topMarginBefore = layoutParamsBefore.topMargin;
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);
                layoutParamsBefore.topMargin = (int) ((1 - interpolatedTime) * topMarginBefore);
                view.setLayoutParams(layoutParamsBefore);
            }
        };
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                afterAnimation.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animation.setDuration(msDuration);
        view.startAnimation(animation);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (hello != null)
            if (isVisibleToUser) {
                firstAnimation(hello, () -> and_welcome_to_baldphone.setVisibility(View.VISIBLE), 2000);
            }
    }

    @Override
    protected void actualSetup() {
        firstAnimation(hello, () -> and_welcome_to_baldphone.setVisibility(View.VISIBLE), 2000);
    }

    @Override
    protected int layoutRes() {
        return R.layout.tutorial_fragment_1;
    }
}