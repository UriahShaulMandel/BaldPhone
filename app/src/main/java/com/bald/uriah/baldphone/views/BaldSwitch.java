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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bald.uriah.baldphone.R;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class BaldSwitch extends LinearLayout {
    private static final String TAG = BaldSwitch.class.getSimpleName();
    private @ColorInt
    int textColorOnSelected;
    private @ColorInt
    int textColorOnButton;
    private float size;
    private boolean enabled;
    private boolean checked;
    private Context context;
    private OnChangeListener onChangeListener = new OnChangeListener() {
        @Override
        public void onChange(boolean isChecked) {
        }
    };
    private CharSequence yes, no;

    private TextView tv_yes, tv_no;

    public BaldSwitch(Context context) {
        super(context);
        init(context, null);
    }

    public BaldSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    public BaldSwitch(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init(context, attributeSet);
    }

    public BaldSwitch(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        init(context, attributeSet);
    }


    private void init(Context context, @Nullable AttributeSet attributeSet) {
        this.context = context;
        setOrientation(LinearLayout.HORIZONTAL);

        final TypedArray styleAttributesArray = context.obtainStyledAttributes(attributeSet, R.styleable.BaldSwitch);
        size = styleAttributesArray.getDimension(R.styleable.BaldSwitch_size, 0);
        yes = styleAttributesArray.getString(R.styleable.BaldSwitch_yes);
        if (yes == null)
            yes = context.getText(R.string.yes);

        no = styleAttributesArray.getString(R.styleable.BaldSwitch_no);
        if (no == null)
            no = context.getText(R.string.no);

        //Dont change, trust me
        enabled = (styleAttributesArray.getBoolean(R.styleable.BaldSwitch_enabled, true));
        setEnabled(enabled);
        //Dont change, trust me

        checked = styleAttributesArray.getBoolean(R.styleable.BaldSwitch_checked, false);
        styleAttributesArray.recycle();

        try {
            final TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.bald_text_on_selected, typedValue, true);
            textColorOnSelected = typedValue.data;
            theme.resolveAttribute(R.attr.bald_text_on_button, typedValue, true);
            textColorOnButton = typedValue.data;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            textColorOnSelected = Color.BLACK;
            textColorOnButton = Color.BLACK;
        }
        updateView();

    }


    private void updateView() {
        if (tv_yes == null && tv_no == null)//First Call
        {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            tv_yes = (TextView) inflater.inflate(R.layout.bald_switch_yes, this, false);
            tv_no = (TextView) inflater.inflate(R.layout.bald_switch_no, this, false);

            setPadding(2, 2, 2, 2);
            setOrientation(HORIZONTAL);


            tv_yes.setText(yes);
            tv_yes.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!enabled)
                        throw new IllegalStateException("should not be clicked!");
                    if (checked)
                        return;
                    checked = true;
                    onChangeListener.onChange(checked);
                    updateView();
                }
            });

            tv_no.setText(no);
            tv_no.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!enabled)
                        throw new IllegalStateException("should not be clicked!");
                    if (!checked)
                        return;
                    checked = false;
                    onChangeListener.onChange(checked);
                    updateView();
                    return;
                }
            });

            if (size == 0) {
                tv_yes.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small));
                tv_no.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small));

            } else {
                tv_yes.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                tv_no.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }


            addView(tv_yes);
            addView(tv_no);
        }

        tv_yes.setEnabled(enabled);
        tv_no.setEnabled(enabled);


        if (enabled) {


            setBackground(ContextCompat.getDrawable(context, R.drawable.btn_enabled));
            if (checked) {
                tv_yes.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_selected_no_border));
                tv_no.setBackground(ContextCompat.getDrawable(context, R.drawable.style_for_buttons_no_border));
                tv_yes.setTextColor(textColorOnSelected);
                tv_no.setTextColor(textColorOnButton);

            } else {
                tv_yes.setBackground(ContextCompat.getDrawable(context, R.drawable.style_for_buttons_no_border));
                tv_no.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_selected_no_border));
                tv_yes.setTextColor(textColorOnButton);
                tv_no.setTextColor(textColorOnSelected);
            }


        } else {
            setBackground(ContextCompat.getDrawable(context, R.drawable.btn_disabled));
            tv_yes.setBackground(ContextCompat.getDrawable(context, checked ? R.drawable.btn_disabled : android.R.color.transparent));
            tv_no.setBackground(ContextCompat.getDrawable(context, !checked ? R.drawable.btn_disabled : android.R.color.transparent));

        }

    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled)
            return;

        this.enabled = enabled;
        super.setEnabled(enabled);
//        for(int i=0;i<getChildCount();i++){
//            getChildAt(i).setEnabled(enabled);
//        }
    }

    public OnChangeListener getOnChangeListener() {
        return onChangeListener;
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public boolean isChecked() {
        return checked;
    }

    /**
     * WARNING
     * calling this method WILL NOT call yes/no Method
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
        updateView();
    }

    public interface OnChangeListener {
        void onChange(boolean isChecked);
    }


}
