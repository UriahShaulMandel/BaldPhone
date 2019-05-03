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

package com.bald.uriah.baldphone.views.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.HomeScreenActivity;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.Toggeler;
import com.bald.uriah.baldphone.views.BaldPictureTextButton;

import java.lang.ref.WeakReference;

public class NotesView extends HomeView {
    public static final String TAG = NotesView.class.getSimpleName();
    private EditText editText;
    private SharedPreferences sharedPreferences;

    public NotesView(@NonNull HomeScreenActivity activity) {
        super(activity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        homeScreen.recognizerManager.setNotesFragment(this);

        sharedPreferences =
                getContext()
                        .getSharedPreferences(BPrefs.KEY, Context.MODE_PRIVATE);
        if (sharedPreferences == null) throw new AssertionError();
        final View view = inflater.inflate(R.layout.notes_fragment, container, false);
        editText = view.findViewById(R.id.edit_text);
        editText.setText(sharedPreferences.getString(BPrefs.NOTE_KEY, ""));

        final BaldPictureTextButton bt_edit = view.findViewById(R.id.bt_edit);
        Toggeler.newSimpleTextImageToggeler(
                bt_edit,
                bt_edit.getImageView(),
                bt_edit.getTextView(),
                R.drawable.edit_on_button,
                R.drawable.check_on_button,
                R.string.edit,
                R.string.done,
                (v -> {
                    editText.setEnabled(true);
                    if (editText.requestFocus()) {
                        editText.setSelection(editText.getText().length());
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

                    }
                }),
                (v -> editText.setEnabled(false))
        );
        view.findViewById(R.id.bt_speak).setOnClickListener((v) -> {
            if (homeScreen.recognizerManager != null)
                homeScreen.recognizerManager.displaySpeechRecognizer();
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPreferences.edit().putString(BPrefs.NOTE_KEY, s.toString()).apply();
            }
        });
        return view;
    }

    public void onSpeechRecognizerResult(final String spokenText) {
        editText.append("\n");
        editText.append(spokenText);
        sharedPreferences.edit().putString(BPrefs.NOTE_KEY, editText.getText().toString()).apply();
    }

    public static class RecognizerManager {
        private WeakReference<HomeScreenActivity> homeScreen;
        private WeakReference<NotesView> notesFragment;

        public void displaySpeechRecognizer() {
            if (assertOk())
                homeScreen.get().displaySpeechRecognizer();

        }

        public void onSpeechRecognizerResult(String spokenText) {
            if (assertOk())
                notesFragment.get().onSpeechRecognizerResult(spokenText);
        }

        public boolean assertOk() {
            return homeScreen != null && notesFragment != null && homeScreen.get() != null && notesFragment.get() != null;
        }

        public void setHomeScreen(HomeScreenActivity homeScreen) {
            this.homeScreen = new WeakReference<>(homeScreen);
        }

        public void setNotesFragment(NotesView notesFragment) {
            this.notesFragment = new WeakReference<>(notesFragment);
        }
    }

}
