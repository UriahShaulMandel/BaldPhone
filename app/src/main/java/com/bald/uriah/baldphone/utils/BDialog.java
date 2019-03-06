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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.views.BaldButton;
import com.bald.uriah.baldphone.views.BaldImageButton;
import com.bald.uriah.baldphone.views.BaldMultipleSelection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.bald.uriah.baldphone.utils.BDialog.DialogState.INPUT_OK_CANCEL;
import static com.bald.uriah.baldphone.utils.BDialog.DialogState.OK;
import static com.bald.uriah.baldphone.utils.BDialog.DialogState.OK_CANCEL;
import static com.bald.uriah.baldphone.utils.BDialog.DialogState.OK_NO;
import static com.bald.uriah.baldphone.utils.BDialog.DialogState.OPTION_OPTION_OK_CANCEL;
import static com.bald.uriah.baldphone.utils.BDialog.DialogState.YES_CANCEL;
import static com.bald.uriah.baldphone.utils.BDialog.DialogState.YES_NO;

public class BDialog extends Dialog {
    public static final float DIM_LEVEL = 0.9f;
    private static final String TAG = BDialog.class.getSimpleName();
    private final Context context;
    private final CharSequence title;
    private final CharSequence subText;
    private final CharSequence options[];
    private final int optionsStartingIndex;
    private final int dialogState;
    private final DialogBoxListener positive, negative, cancel;
    private final boolean cancelable;
    private final int inputType;

    @Nullable
    private final View extraView;


    //Views
    private EditText editText;
    private TextView tv_title, tv_subtext;
    private BaldButton bt_positive, bt_negative;
    private BaldImageButton bt_cancel;
    private ViewGroup container, ll;

    private BDialog(@NonNull Context context,
                    @DialogState int dialogState,
                    @NonNull CharSequence title,
                    @NonNull CharSequence subText,
                    boolean cancelable,
                    @Nullable CharSequence[] options,
                    @Nullable DialogBoxListener positive,
                    @Nullable DialogBoxListener negative,
                    @Nullable final DialogBoxListener cancel,
                    int inputType,
                    int optionsStartingIndex,
                    @Nullable View extraView

    ) {
        super(context, cancelable, null);
        this.cancelable = cancelable;
        this.context = context;
        this.options = options;
        this.subText = subText;
        this.dialogState = dialogState;
        this.title = title;
        this.positive = positive;
        this.negative = negative;
        this.cancel = cancel;
        this.inputType = inputType;
        this.optionsStartingIndex = optionsStartingIndex;
        this.extraView = extraView;

    }

    public static BDialog newInstance(BDB bdb) {
        if (bdb.context == null || bdb.dialogState == 0 || bdb.title == null || bdb.subText == null)
            throw new NullPointerException("bdb.activity, bdb.dialogState, bdb.title, bdb.subText cannot be null! perhaps forgot to setContext() on BDB");

        final BDialog bDialog = BDialog.newInstance(bdb.context, bdb.dialogState, bdb.title, bdb.subText, bdb.cancelable, bdb.options, bdb.positiveButtonListener, bdb.negativeButtonListener, bdb.cancelButtonListener, bdb.inputType, bdb.optionsStartingIndex, bdb.extraView);
        if (bdb.baldActivityToAutoDismiss != null) {
            bdb.baldActivityToAutoDismiss.autoDismissDialog(bDialog);
        }
        return bDialog;
    }

    public static BDialog newInstance(@NonNull Context context,
                                      @DialogState int dialogState,
                                      @NonNull CharSequence title,
                                      @NonNull CharSequence subText,
                                      boolean cancelable,
                                      @Nullable CharSequence[] options,
                                      @Nullable DialogBoxListener positive,
                                      @Nullable DialogBoxListener negative,
                                      @Nullable final DialogBoxListener cancel,
                                      int inputType,
                                      int optionsStartingIndex,
                                      @Nullable View extraView
    ) {
        final BDialog baldDialogBox = new BDialog(context, dialogState, title, subText, cancelable, options, positive, negative, cancel, inputType, optionsStartingIndex, extraView);
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
        if (cancelable) {
            bt_cancel = (BaldImageButton) LayoutInflater.from(context).inflate(R.layout.bald_dialog_box_close_button, container, false);
            bt_cancel.setOnClickListener(v -> {
                if (cancel == null)
                    cancel();
                else if (cancel.activate()) {
                    cancel();
                }
            });

            container.addView(bt_cancel);
        }

        switch (dialogState) {
            case DialogState.OK:
                bt_negative.setVisibility(View.GONE);
                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate()) {
                        cancel();
                    }
                });
                bt_positive.setText(R.string.ok);
                setLeftMargin(bt_positive);

                break;
            case OK_NO:
                bt_positive.setText(R.string.ok);
            case YES_NO:
                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate()) {
                        cancel();
                    }
                });
                bt_negative.setOnClickListener(v -> {
                    if (negative == null)
                        cancel();
                    else if (negative.activate()) {
                        cancel();
                    }
                });

                break;
            case OK_CANCEL:
                bt_positive.setText(R.string.ok);
            case YES_CANCEL:
                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate()) {
                        cancel();
                    }
                });
                bt_negative.setOnClickListener(v -> {
                    if (cancel == null)
                        cancel();
                    else if (cancel.activate()) {
                        cancel();
                    }
                });
                bt_negative.setText(R.string.cancel);


                break;
            case INPUT_OK_CANCEL:
                editText = ll.findViewById(R.id.edit_text);
                editText.setVisibility(View.VISIBLE);
                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate(editText.getText())) {
                        cancel();
                    }
                });
                bt_negative.setOnClickListener(v -> {
                    if (cancel == null)
                        cancel();
                    else if (cancel.activate()) {
                        cancel();
                    }
                });
                bt_cancel.setOnClickListener(v -> {
                    if (cancel == null)
                        cancel();
                    else if (cancel.activate()) {
                        cancel();
                    }
                });
                bt_positive.setText(R.string.ok);
                bt_negative.setText(R.string.cancel);
                break;
            case OPTION_OPTION_OK_CANCEL:
                final BaldMultipleSelection baldMultipleSelection = (BaldMultipleSelection) LayoutInflater.from(context).inflate(R.layout.bald_dialog_box_multiple_selection_view, ll, false);
                baldMultipleSelection.setOrientation(LinearLayout.HORIZONTAL);
                for (CharSequence option : options)
                    baldMultipleSelection.addSelection(option);
                baldMultipleSelection.setSelection(optionsStartingIndex);
                final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, context.getResources().getDisplayMetrics()));
                ll.addView(baldMultipleSelection, 2, layoutParams);

                bt_positive.setOnClickListener(v -> {
                    if (positive == null)
                        cancel();
                    else if (positive.activate(baldMultipleSelection.getSelection())) {
                        cancel();
                    }
                });
                bt_negative.setOnClickListener(v -> {
                    if (cancel == null)
                        cancel();
                    else if (cancel.activate(baldMultipleSelection.getSelection())) {
                        cancel();
                    }
                });
                bt_positive.setText(R.string.ok);
                bt_negative.setText(R.string.cancel);

                break;

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


    public interface DialogBoxListener {
        DialogBoxListener EMPTY = params -> true;
        /**
         * @param params - when Integer its which selection was chosen
         *               when CharSequence its user input
         * @return true if dialog job is finished
         */
        boolean activate(@NonNull Object... params);
    }

    @IntDef({
            YES_CANCEL,
            OK_CANCEL,
            YES_NO,
            OK,
            INPUT_OK_CANCEL,
            OPTION_OPTION_OK_CANCEL,
            OK_NO
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DialogState {
        int
                YES_CANCEL = 1,
                OK_CANCEL = 2,
                OK_NO = 7,
                YES_NO = 3,
                OK = 4,
                INPUT_OK_CANCEL = 5,
                OPTION_OPTION_OK_CANCEL = 6;
    }
}