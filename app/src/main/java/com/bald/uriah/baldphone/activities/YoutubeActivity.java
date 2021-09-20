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
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.DateTimeUtils;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.bald.uriah.baldphone.views.FirstPageAppIcon;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class YoutubeActivity extends BaldActivity {
    public static final String EXTRA_ID = "EXTRA_ID";
    private static final float VERTICAL_BIAS_AFTER = 0.8f;
    private static final float VERTICAL_BIAS_BEFORE = 1f;
    private final static long ANIMATION_DURATION = DateTimeUtils.MILLISECOND * 800;

    private Space bottom;
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int index = getIntent().getIntExtra(EXTRA_ID, -1);
        if (index == -1)
            throw new IllegalArgumentException("must has id");

        setContentView(R.layout.youtube_container);

        ((BaldTitleBar) findViewById(R.id.bald_title_bar)).getBt_help().setVisibility(View.INVISIBLE);

        final Resources resources = getResources();
        url = resources.getStringArray(R.array.yt_links)[index];

        final int[]
                related = S.intArrFromString(resources.getStringArray(R.array.yt_related)[index]),
                otherTexts = S.typedArrayToResArray(resources, R.array.yt_texts),
                otherLogos = S.typedArrayToResArray(resources, R.array.yt_logos),
                otherBackground = S.typedArrayToResArray(resources, R.array.yt_background);

        final View[] bt_colors = new View[]{
                findViewById(R.id.bt_left_color),
                findViewById(R.id.bt_right_color)
        };
        final FirstPageAppIcon[] bts = new FirstPageAppIcon[]{
                findViewById(R.id.bt_left),
                findViewById(R.id.bt_right)
        };

        bottom = findViewById(R.id.bottom);

        for (int i = 0; i < bts.length; i++) {
            final int currRelated = related[i];
            bt_colors[i].setBackgroundResource(otherBackground[currRelated]);
            bts[i].setOnClickListener(v -> {
                startActivity(
                        new Intent(this, YoutubeActivity.class)
                                .putExtra(YoutubeActivity.EXTRA_ID, currRelated)
                );
                finish();
            });
            bts[i].setImageResource(otherLogos[currRelated]);
            bts[i].setText(otherTexts[currRelated]);
        }

        final YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_fragment);
        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull final YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(url, 0);
                youTubePlayer.play();
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                if (state == PlayerConstants.PlayerState.ENDED) {
                    if (bottom == null)
                        return;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) bottom.getLayoutParams();
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            params.verticalBias = ((interpolatedTime) * (VERTICAL_BIAS_AFTER - VERTICAL_BIAS_BEFORE)) + VERTICAL_BIAS_BEFORE;
                            bottom.setLayoutParams(params);
                        }
                    };
                    animation.setDuration(ANIMATION_DURATION);
                    bottom.startAnimation(animation);
                } else if (state == PlayerConstants.PlayerState.PLAYING) {
                    if (bottom == null)
                        return;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) bottom.getLayoutParams();
                    if (params.verticalBias == VERTICAL_BIAS_BEFORE)
                        return;
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            params.verticalBias = ((interpolatedTime) * (VERTICAL_BIAS_BEFORE - VERTICAL_BIAS_AFTER)) + VERTICAL_BIAS_AFTER;
                            bottom.setLayoutParams(params);
                        }
                    };
                    animation.setDuration(ANIMATION_DURATION);
                    bottom.startAnimation(animation);
                }
            }
        });

    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
