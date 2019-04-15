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

package com.bald.uriah.baldphone.activities;

import android.os.Bundle;
import android.widget.EditText;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.BaldToast;

import org.acra.ACRA;

import androidx.annotation.Nullable;

public class FeedbackActivity extends BaldActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        final EditText et_feedback = findViewById(R.id.et_feedback);
        findViewById(R.id.bt_send).setOnClickListener(v -> {
            final CharSequence text = et_feedback.getText();
            if (text.length() == 1)
                BaldToast.from(v.getContext()).setType(BaldToast.TYPE_ERROR).setText(R.string.feedback_cannot_be_empty).show();
            else {
                ACRA.getErrorReporter().handleSilentException(new FeedbackException(String.valueOf(text)));
                BaldToast.from(getApplicationContext()).setText(R.string.feedback_sent_successfully).show();
                finish();
            }
        });
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }

    private final static class FeedbackException extends Exception {
        FeedbackException(String message) {
            super(message);
        }
    }
}
