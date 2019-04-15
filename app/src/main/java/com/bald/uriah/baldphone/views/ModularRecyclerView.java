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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.D;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

//most probably must be match parent! remember!
public class ModularRecyclerView extends RecyclerView implements Modular {
    public boolean touchEnabled;


    public ModularRecyclerView(Context context) {
        super(context);
        touchEnabled = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE).getBoolean(BPrefs.TOUCH_NOT_HARD_KEY, false);
    }

    public ModularRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        touchEnabled = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE).getBoolean(BPrefs.TOUCH_NOT_HARD_KEY, false);
    }

    public ModularRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        touchEnabled = context.getSharedPreferences(D.BALD_PREFS, Context.MODE_PRIVATE).getBoolean(BPrefs.TOUCH_NOT_HARD_KEY, false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return touchEnabled && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return touchEnabled && super.onTouchEvent(ev);
    }


    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof ModularAdapter) {
            super.setAdapter(adapter);
            final AdapterDataObserver emptyObserver = ((ScrollingHelper) getParent()).emptyObserver;
            adapter.registerAdapterDataObserver(emptyObserver);
            emptyObserver.onChanged();


        } else
            throw new IllegalArgumentException("Adapter must be Modular!, and remember to call super.onBindViewHolder!");
    }


    public static abstract class ModularAdapter<T extends ViewHolder> extends RecyclerView.Adapter<T> {
        //For the future
        @Override
        public void onBindViewHolder(@NonNull T holder, int position) {

        }
    }
}
