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

import android.content.Context;
import android.view.View;

import com.bald.uriah.baldphone.activities.BaldActivity;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * Builder for {@link BDialog}
 */
public class BDB {
    public Context context;

    @BDialog.DialogState
    int dialogState = BDialog.DialogState.OK;
    CharSequence title = "", subText, options[];
    int inputType;
    BDialog.DialogBoxListener positiveButtonListener = BDialog.DialogBoxListener.EMPTY;
    BDialog.DialogBoxListener negativeButtonListener = BDialog.DialogBoxListener.EMPTY;
    BDialog.DialogBoxListener cancelButtonListener = BDialog.DialogBoxListener.EMPTY;
    boolean cancelable = true;
    BDialog.StartingIndexChooser startingIndexChooser = ()->0;
    BaldActivity baldActivityToAutoDismiss;
    @Nullable
    View extraView;

    private BDB() {
    }

    public static BDB from(@Nullable Context context) {
        BDB bdb = new BDB();
        bdb.context = context;
        if (context instanceof BaldActivity) {
            bdb.baldActivityToAutoDismiss = (BaldActivity) context;
        }
        return bdb;
    }

    public BDB setDialogState(@BDialog.DialogState int dialogState) {
        this.dialogState = dialogState;
        return this;
    }

    public BDB setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public BDB setTitle(@StringRes int titleId) {
        return setTitle(context.getText(titleId));
    }

    public BDB setSubText(CharSequence subText) {
        this.subText = subText;
        return this;
    }

    public BDB setSubText(@StringRes int subTextId) {
        return setSubText(context.getText(subTextId));
    }


    public BDB setOptions(CharSequence... options) {
        dialogState = BDialog.DialogState.OPTION_OPTION_OK_CANCEL;
        this.options = options;
        return this;
    }

    public BDB setOptions(@StringRes int... options) {
        final CharSequence[] charSequences = new CharSequence[options.length];
        for (int i = 0; i < options.length; i++)
            charSequences[i] = context.getText(options[i]);

        return setOptions(charSequences);
    }

    public BDB setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public BDB setPositiveButtonListener(@Nullable BDialog.DialogBoxListener dialogBoxListener) {
        this.positiveButtonListener = dialogBoxListener;
        return this;
    }

    public BDB setNegativeButtonListener(@Nullable BDialog.DialogBoxListener dialogBoxListener) {
        this.negativeButtonListener = dialogBoxListener;
        return this;
    }

    public BDB setCancelButtonListener(@Nullable BDialog.DialogBoxListener dialogBoxListener) {
        this.cancelButtonListener = dialogBoxListener;
        return this;
    }

    public BDB setInputType(int inputType) {
        this.inputType = inputType;
        return this;
    }

    public BDB setOptionsStartingIndex(BDialog.StartingIndexChooser startingIndexChooser) {
        this.startingIndexChooser = startingIndexChooser;
        return this;
    }

    public BDB setExtraView(@Nullable View extraView) {
        this.extraView = extraView;
        return this;
    }

    public BDB setBaldActivityToAutoDismiss(BaldActivity baldActivityToAutoDismiss) {
        this.baldActivityToAutoDismiss = baldActivityToAutoDismiss;
        return this;
    }

    public BDialog show() {
        return BDialog.newInstance(this);
    }
}