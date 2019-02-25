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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.D;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ScrollingHelper extends ConstraintLayout {

    public static final int START = -3;
    public static final int RIGHT = 2;
    public static final int END = 3;
    public static final int LEFT = -2;
    public static final int UP = 1;
    public static final int DOWN = -1;
    public static final int NO = 0;

    public static final int TOP_AND_BOTTOM = 0;


    @IntDef({RIGHT, LEFT, UP, DOWN, NO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
    }

    @IntDef({END, RIGHT, START, LEFT, TOP_AND_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IntWhereBar {
    }


    @IntWhereBar
    private int whereBar = RIGHT;

    private DisplayMetrics displayMetrics;

    private static final String TAG = ScrollingHelper.class.getSimpleName();
    public final int verticalScrollerLength = 70;//in dp lowercase cause in future may be argument
    public final int topAndBottomScrollerLength = 50;//in dp lowercase cause in future may be argument
    private SharedPreferences sharedPreferences;
    private LayoutInflater layoutInflater;
    private Context context;
    private View child;
    private String empty;

    private boolean aViewAdded = false,
            programmerAwareOfUsingNonModularViews = false,
            gone,
            horizontalScrolling = false;

    @IdRes
    private int childId;

    @IdRes
    private static final int containerId = R.id.container;

    private int SCROLL_CONST;
    private static final float SCROLL_CONST_IN_DP = 9;
    @Direction
    private int direction;

    private Choreographer choreographer = Choreographer.getInstance();
    private Choreographer.FrameCallback frameCallback;

    public ScrollingHelper(Context context) {
        super(context);
        init(context, null);
    }

    public ScrollingHelper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScrollingHelper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, @Nullable AttributeSet attributeSet) {
        this.context = context;
        this.displayMetrics = context.getResources().getDisplayMetrics();
        this.layoutInflater = LayoutInflater.from(context);

        //if touch is hard
        this.sharedPreferences = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE);
        gone = sharedPreferences.getBoolean(BPrefs.TOUCH_NOT_HARD_KEY, false);

        SCROLL_CONST = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SCROLL_CONST_IN_DP, getResources().getDisplayMetrics());
        //add the helper
        getAttributeSet(attributeSet);
        assertion();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child.getId() == R.id.empty_view) {
            if (this.emptyView != null)
                removeView(this.emptyView);
            this.emptyView = child;
            super.addView(this.emptyView, index, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }
        super.addView(child, index, params);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (aViewAdded || gone) {
            int id = child.getId();
            ConstraintSet constraintSet = new ConstraintSet();
//            constraintSet.clone(this);
            constraintSet.constrainHeight(id, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.constrainWidth(id, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.connect(id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            constraintSet.connect(id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            constraintSet.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.setElevation(id, 2);
            constraintSet.setTranslationZ(id, 2);

            constraintSet.applyTo(this);


            if (aViewAdded)
                return;

        }
        aViewAdded = true;

        this.child = getChildAt(0);
        if (!(this.child instanceof Modular) && !programmerAwareOfUsingNonModularViews) {
            throw new IllegalArgumentException("not modular view inside a scrollingHelper");
        }
        if (child instanceof ModularRecyclerView) {
            if (emptyView == null) {
                this.emptyView = layoutInflater.inflate(R.layout.empty_view, this, false);
                super.addView(emptyView);
            }

        }

        if (!gone) {
            childId = child.getId();
            if (childId == View.NO_ID) {
                child.setId(childId = R.id.child);
            }
            scrollerHandler();
        }

    }

    private void setArrowsVisibility(boolean visible) {
        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, visible ? verticalScrollerLength : 0, displayMetrics);

        ConstraintSet constraintSet = new ConstraintSet();
        if (whereBar == RIGHT) {
            constraintSet.constrainHeight(childId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.constrainWidth(childId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.connect(childId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            constraintSet.connect(childId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            constraintSet.connect(childId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(childId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.setMargin(childId, ConstraintSet.RIGHT, width);

            constraintSet.constrainHeight(containerId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.constrainWidth(containerId, width);
            constraintSet.connect(containerId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            constraintSet.connect(containerId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(containerId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);


        } else {
            constraintSet.constrainHeight(childId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.constrainWidth(childId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.connect(childId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            constraintSet.connect(childId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            constraintSet.connect(childId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(childId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.setMargin(childId, ConstraintSet.LEFT, width);

            constraintSet.constrainHeight(containerId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.constrainWidth(containerId, width);
            constraintSet.connect(containerId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            constraintSet.connect(containerId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(containerId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        }
        constraintSet.applyTo(this);


    }

    private void getAttributeSet(@Nullable AttributeSet attributeSet) {
        if (attributeSet == null)
            return;
        final TypedArray styleAttributesArray = context.obtainStyledAttributes(attributeSet, R.styleable.ScrollingHelper);
        horizontalScrolling = styleAttributesArray.getBoolean(R.styleable.ScrollingHelper_horizontal_scrolling, false);
        empty = styleAttributesArray.getString(R.styleable.ScrollingHelper_empty_text);
        if (empty == null)
            empty = context.getString(R.string.nothing);
        whereBar =
                styleAttributesArray.getInt(R.styleable.ScrollingHelper_where_bar,
                        sharedPreferences.getBoolean(BPrefs.RIGHT_HANDED_KEY, BPrefs.RIGHT_HANDED_DEFAULT_VALUE)
                                ?
                                RIGHT
                                :
                                LEFT
                );
        if (whereBar == END)
            whereBar = getResources().getBoolean(R.bool.is_right_to_left) ? LEFT : RIGHT;
        else if (whereBar == START)
            whereBar = getResources().getBoolean(R.bool.is_right_to_left) ? RIGHT : LEFT;

        programmerAwareOfUsingNonModularViews =
                styleAttributesArray.getBoolean(R.styleable.ScrollingHelper_im_aware_this_inner_view_isnt_modular, false);
        styleAttributesArray.recycle();
    }

    private void assertion() {
        if (whereBar != RIGHT && whereBar != LEFT && whereBar != TOP_AND_BOTTOM)
            throw new IllegalArgumentException("where_bar must be Right(2) or Left(-2) or Top And Bottom(0)");

        if (whereBar == TOP_AND_BOTTOM && horizontalScrolling)
            throw new IllegalArgumentException("top and bottom mode doesn't support horizontal scrolling!");

        if (horizontalScrolling)
            throw new IllegalArgumentException("Horizontal Scrolling is not yet supported!");

    }


    @SuppressLint("ClickableViewAccessibility")
    private void scrollerHandler() {
        this.child.setOnClickListener(v -> {/*nothing*/});
        if (child instanceof ScrollView) {
            final ScrollView scrollView = (ScrollView) child;
            frameCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    switch (direction) {
                        case UP:
                            scrollView.smoothScrollBy(0, -SCROLL_CONST);
                            break;
                        case DOWN:
                            scrollView.smoothScrollBy(0, SCROLL_CONST);
                            break;
                        case RIGHT:
                            scrollView.smoothScrollBy(SCROLL_CONST, 0);
                            break;
                        case LEFT:
                            scrollView.smoothScrollBy(-SCROLL_CONST, 0);
                            break;
                    }
                    if (direction != NO) {
                        choreographer.postFrameCallback(this);
                    }
                }
            };

        } else
            frameCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    switch (direction) {
                        case RIGHT:
                            child.scrollBy(SCROLL_CONST, 0);
                            break;
                        case LEFT:
                            child.scrollBy(-SCROLL_CONST, 0);
                            break;
                        case UP:
                            child.scrollBy(0, -SCROLL_CONST);
                            break;
                        case DOWN:
                            child.scrollBy(0, SCROLL_CONST);
                            break;

                    }

                    if (direction != NO) {
                        choreographer.postFrameCallback(this);
                    }
                }
            };


        final OnClickListener onClickListener = D.EMPTY_CLICK_LISTENER;


        if (whereBar == RIGHT || whereBar == LEFT) {
            LinearLayout container;

            container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setId(containerId);
            super.addView(container);

            setArrowsVisibility(true);


            if (horizontalScrolling) {
                //will never happen see: private void assertion()
            } else {
                final View up = layoutInflater.inflate(R.layout.scrolling_helper_up, container, false);
                final View down = layoutInflater.inflate(R.layout.scrolling_helper_down, container, false);
                container.addView(up, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
                container.addView(down, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
                up.setOnClickListener(onClickListener);
                down.setOnClickListener(onClickListener);
                up.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                        direction = UP;
                        choreographer.removeFrameCallback(frameCallback);
                        choreographer.postFrameCallback(frameCallback);
                        return false;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP && direction == UP) {
                        direction = NO;
                        return false;
                    }
                    return false;
                });
                down.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                        direction = DOWN;
                        choreographer.removeFrameCallback(frameCallback);
                        choreographer.postFrameCallback(frameCallback);
                        return false;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP && direction == DOWN) {
                        direction = NO;
                        return false;
                    }
                    return false;
                });


            }


        } else if (whereBar == TOP_AND_BOTTOM) {
            final int upContainerId = R.id.container;
            final int downContainerId = R.id.container2;
            final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topAndBottomScrollerLength, displayMetrics);


            final View upContainer = layoutInflater.inflate(R.layout.scrolling_helper_up, this, false);
            final View downContainer = layoutInflater.inflate(R.layout.scrolling_helper_down, this, false);


            upContainer.setId(upContainerId);
            downContainer.setId(downContainerId);

            this.addView(upContainer);
            this.addView(downContainer);
            ConstraintSet constraintSet = new ConstraintSet();

            constraintSet.constrainHeight(childId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.constrainWidth(childId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.connect(childId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            constraintSet.connect(childId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            constraintSet.connect(childId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(childId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.setMargin(childId, ConstraintSet.TOP, height);
            constraintSet.setMargin(childId, ConstraintSet.BOTTOM, height);

            constraintSet.constrainHeight(upContainerId, height);
            constraintSet.constrainWidth(upContainerId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.connect(upContainerId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            constraintSet.connect(upContainerId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(upContainerId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);

            constraintSet.constrainHeight(downContainerId, height);
            constraintSet.constrainWidth(downContainerId, ConstraintSet.MATCH_CONSTRAINT);
            constraintSet.connect(downContainerId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            constraintSet.connect(downContainerId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.connect(downContainerId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);


            constraintSet.applyTo(this);
            upContainer.setOnClickListener(onClickListener);
            downContainer.setOnClickListener(onClickListener);

            upContainer.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    direction = UP;
                    choreographer.removeFrameCallback(frameCallback);
                    choreographer.postFrameCallback(frameCallback);
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_UP && direction == UP) {
                    direction = NO;
                    return false;
                }
                return false;
            });
            downContainer.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    direction = DOWN;
                    choreographer.removeFrameCallback(frameCallback);
                    choreographer.postFrameCallback(frameCallback);
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_UP && direction == DOWN) {
                    direction = NO;
                    return false;
                }
                return false;
            });

        }

        this.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP)
                direction = NO;
            return false;
        });


    }

    private View emptyView;
    public RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (child instanceof RecyclerView) {
                RecyclerView.Adapter<?> adapter = ((RecyclerView) child).getAdapter();
                if (adapter != null && emptyView != null) {
                    if (adapter.getItemCount() == 0) {
                        if (!gone)
                            setArrowsVisibility(false);
                        if (emptyView instanceof TextView) {
                            ((TextView) emptyView).setText(empty);
                        } else
                            emptyView.setVisibility(VISIBLE);
                    } else {
                        if (emptyView instanceof TextView) {
                            ((TextView) emptyView).setText("");
                        } else
                            emptyView.setVisibility(INVISIBLE);
                        if (!gone)
                            setArrowsVisibility(true);
                    }
                }
            }

        }
    };


}
