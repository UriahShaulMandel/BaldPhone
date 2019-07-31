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

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.views.BaldButton;
import com.bald.uriah.baldphone.views.BaldImageButton;
import com.bald.uriah.baldphone.views.BaldMultipleSelection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BDialog extends Dialog {
    private static final String TAG = BDialog.class.getSimpleName();
    public static final int
            FLAG_POSITIVE = 1,
            FLAG_NEGATIVE = 1 << 1,
            FLAG_INPUT = 1 << 8 | FLAG_POSITIVE,
            FLAG_OK = FLAG_POSITIVE | 1 << 2,
            FLAG_YES = FLAG_POSITIVE | 1 << 3,
            FLAG_CANCEL = FLAG_NEGATIVE | 1 << 4,
            FLAG_NO = FLAG_NEGATIVE | 1 << 5,
            FLAG_CUSTOM_NEGATIVE = FLAG_NEGATIVE | 1 << 6,
            FLAG_CUSTOM_POSITIVE = FLAG_POSITIVE | 1 << 7,
            FLAG_OPTIONS = 1 << 9,
            FLAG_NOT_CANCELABLE = 1 << 10;
    private static final float DIM_LEVEL = 0.9f;
    private final Context context;
    private final CharSequence title;
    private final CharSequence subText;
    private final CharSequence[] options;
    private final StartingIndexChooser startingIndexChooser;
    private final DialogBoxListener positive, negative;
    private final int inputType;
    private final int flags;
    private final CharSequence negativeCustomText;
    private final CharSequence positiveCustomText;
    @Nullable
    private final View extraView;
    //Views
    private EditText editText;
    private TextView tv_title, tv_subtext;
    private BaldButton bt_positive, bt_negative;
    private BaldImageButton bt_cancel;
    private ViewGroup container, ll;

    private BDialog(final @NonNull Context context,
                    final @NonNull CharSequence title,
                    final @NonNull CharSequence subText,
                    final @Nullable CharSequence[] options,
                    final @Nullable DialogBoxListener positive,
                    final @Nullable DialogBoxListener negative,
                    final int inputType,
                    final @Nullable StartingIndexChooser startingIndexChooser,
                    final @Nullable View extraView,
                    final @Nullable CharSequence negativeCustomText,
                    final @Nullable CharSequence positiveCustomText,
                    final int flags) {
        super(context);
        this.context = context;
        this.options = options;
        this.subText = subText;
        this.title = title;
        this.positive = positive;
        this.negative = negative;
        this.inputType = inputType;
        this.startingIndexChooser = startingIndexChooser;
        this.extraView = extraView;
        this.negativeCustomText = negativeCustomText;
        this.positiveCustomText = positiveCustomText;

        this.flags = flags;
    }

    public static BDialog newInstance(BDB bdb) {
        if (bdb.context == null || bdb.title == null || bdb.subText == null)
            throw new NullPointerException("bdb.activity, bdb.dialogState, bdb.title, bdb.subText cannot be null! perhaps forgot to setContext() on BDB");

        final BDialog bDialog = BDialog.newInstance(bdb.context, bdb.title, bdb.subText, bdb.options, bdb.positiveButtonListener, bdb.negativeButtonListener, bdb.inputType, bdb.startingIndexChooser, bdb.extraView, bdb.negativeCustomText, bdb.positiveCustomText, bdb.flags);
        if (bdb.baldActivityToAutoDismiss != null) {
            bdb.baldActivityToAutoDismiss.autoDismiss(bDialog);
        }
        return bDialog;
    }

    public static BDialog newInstance(final @NonNull Context context,
                                      final @NonNull CharSequence title,
                                      final @NonNull CharSequence subText,
                                      final @Nullable CharSequence[] options,
                                      final @Nullable DialogBoxListener positive,
                                      final @Nullable DialogBoxListener negative,
                                      final int inputType,
                                      final StartingIndexChooser startingIndexChooser,
                                      final @Nullable View extraView,
                                      final @Nullable CharSequence negativeCustomText,
                                      final @Nullable CharSequence positiveCustomText,
                                      final int flags
    ) {
        final BDialog baldDialogBox = new BDialog(context, title, subText, options, positive, negative, inputType, startingIndexChooser, extraView, negativeCustomText, positiveCustomText, flags);
        baldDialogBox.show();
        Window window = baldDialogBox.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // This flag is required to set otherwise the setDimAmount method will not show any effect
        window.setDimAmount(DIM_LEVEL);
        return baldDialogBox;
    }

    private static void setLeftMargin(View view) {
        final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMarginEnd(layoutParams.getMarginStart());
        view.setLayoutParams(layoutParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.new_bald_dialog_box);
        attachXml();
        tv_title.setText(title);
        tv_subtext.setText(subText);
        if (!containFlag(FLAG_NOT_CANCELABLE)) {
            bt_cancel = (BaldImageButton) LayoutInflater.from(context).inflate(R.layout.bald_dialog_box_close_button, container, false);
            View.OnClickListener cancelClickListener = v -> {
                if (negative == null)
                    cancel();
                else if (negative.activate()) {
                    cancel();
                }
            };
            bt_cancel.setOnClickListener(cancelClickListener);
            if (containFlag(FLAG_CANCEL))
                bt_negative.setOnClickListener(cancelClickListener);
            container.addView(bt_cancel);
        } else
            setCancelable(false);
        if (containFlag(FLAG_POSITIVE)) {
            if (containFlag(FLAG_INPUT)) {
                editText = ll.findViewById(R.id.edit_text);
                editText.setVisibility(View.VISIBLE);
                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate(editText.getText()))
                        cancel();

                });
            } else {
                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate())
                        cancel();
                });
            }
            if (containFlag(FLAG_OK))
                bt_positive.setText(R.string.ok);
            else if (containFlag(FLAG_CUSTOM_POSITIVE))
                bt_positive.setText(positiveCustomText);
        }
        if (containFlag(FLAG_NEGATIVE)) {
            bt_negative.setOnClickListener(v -> {
                if (negative == null)
                    cancel();
                else if (negative.activate())
                    cancel();
            });
            if (containFlag(FLAG_CANCEL))
                bt_negative.setText(R.string.cancel);
            else if (containFlag(FLAG_CUSTOM_NEGATIVE))
                bt_negative.setText(negativeCustomText);
        } else {
            bt_negative.setVisibility(View.GONE);
            setLeftMargin(bt_positive);
        }

        if (containFlag(FLAG_OPTIONS)) {

            final BaldMultipleSelection baldMultipleSelection = (BaldMultipleSelection) LayoutInflater.from(context).inflate(R.layout.bald_dialog_box_multiple_selection_view, ll, false);
            baldMultipleSelection.setOrientation(LinearLayout.HORIZONTAL);
            for (final CharSequence option : options)
                baldMultipleSelection.addSelection(option);
            baldMultipleSelection.setSelection(startingIndexChooser.chooseStartingIndex());
            final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, context.getResources().getDisplayMetrics()));
            ll.addView(baldMultipleSelection, 2, layoutParams);
            if (containFlag(FLAG_POSITIVE))
                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate(baldMultipleSelection.getSelection()))
                        cancel();

                });
            if (containFlag(FLAG_NEGATIVE))
                bt_negative.setOnClickListener(v -> {
                    if (negative == null)
                        cancel();
                    else if (negative.activate(baldMultipleSelection.getSelection()))
                        cancel();
                });
        }
    }

    private void attachXml() {
        this.container = findViewById(R.id.container);
        this.ll = findViewById(R.id.ll);
        this.tv_title = findViewById(R.id.dialog_box_title);
        this.tv_subtext = findViewById(R.id.dialog_box_text);
        this.bt_positive = findViewById(R.id.dialog_box_true);
        this.bt_negative = findViewById(R.id.dialog_box_false);
        this.bt_cancel = findViewById(R.id.iv_close);

        if (this.extraView != null) {
            if (this.extraView.getParent() != null) {
                ((ViewGroup) extraView.getParent()).removeView(extraView);
            }
            ((FrameLayout) findViewById(R.id.frame_layout))
                    .addView(extraView,
                            new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT)
                    );
        }
    }

    public boolean containFlag(@BDFlags int flag) {
        return (flags | flag) == flags;
    }

    public interface StartingIndexChooser {
        int chooseStartingIndex();
    }

    public interface DialogBoxListener {
        DialogBoxListener EMPTY = params -> true;

        /**
         * @param params - when Integer its which selection was chosen
         *               when CharSequence its user input
         * @return true if dialog job is finished
         */
        boolean activate(@NonNull Object... params);
    }

    @IntDef({FLAG_POSITIVE, FLAG_NEGATIVE, FLAG_INPUT, FLAG_OPTIONS, FLAG_OK, FLAG_YES, FLAG_CANCEL, FLAG_NO, FLAG_CUSTOM_NEGATIVE, FLAG_CUSTOM_POSITIVE, FLAG_NOT_CANCELABLE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface BDFlags {
    }

}