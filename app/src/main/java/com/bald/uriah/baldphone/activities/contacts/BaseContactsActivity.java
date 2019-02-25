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

package com.bald.uriah.baldphone.activities.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.adapters.ContactRecyclerViewAdapter;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.utils.SoftInputAssist;
import com.bald.uriah.baldphone.utils.Toggeler;
import com.bald.uriah.baldphone.views.BaldTitleBar;

import static android.view.View.GONE;

/**
 * Base activity for {@link ShareActivity} and {@link ContactsActivity}.
 * both has {@link R.layout#contacts_search} in their layout.
 */
abstract class BaseContactsActivity extends BaldActivity {
    private static final String TAG = BaseContactsActivity.class.getSimpleName();
    public static final int SPEECH_REQUEST_CODE = 5678;
    public static final String INTENT_EXTRA_CONTACT_ADAPTER_MODE = "INTENT_EXTRA_CONTACT_ADAPTER_MODE";

    //<views>
    protected EditText et_filter_input;
    protected ImageView bt_speak, bt_favorite, bt_type;
    protected RecyclerView recyclerView;
    protected BaldTitleBar baldTitleBar;
    //</views>

    protected ContentResolver contentResolver;
    private String filter = "";
    private boolean favorite = false;
    protected ContactRecyclerViewAdapter contactRecyclerViewAdapter;
    protected int mode = ContactRecyclerViewAdapter.MODE_DEFAULT;

    private SoftInputAssist softInputAssist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        setContentView(layout());

        softInputAssist = new SoftInputAssist(this);
        contentResolver = getContentResolver();
        attachXml();
        viewsInit();


        final Intent callingIntent = getIntent();
        if (callingIntent != null) {
            mode = callingIntent.getIntExtra(INTENT_EXTRA_CONTACT_ADAPTER_MODE, mode);
        }

        init();

        applyFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (softInputAssist != null)
            softInputAssist.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (softInputAssist != null)
            softInputAssist.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (softInputAssist != null)
            softInputAssist.onDestroy();
    }

    protected abstract void init();

    @LayoutRes
    protected abstract int layout();

    protected void attachXml() {
        baldTitleBar = findViewById(R.id.bald_title_bar);
        et_filter_input = findViewById(R.id.edit_text);
        recyclerView = findViewById(R.id.contacts_recycler_view);
        bt_speak = findViewById(R.id.bt_speak);
        bt_type = findViewById(R.id.bt_type);
        bt_favorite = findViewById(R.id.bt_favorite);
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.ll_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);


    }

    protected void viewsInit() {
        et_filter_input.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        filter = et_filter_input.getText().toString().toLowerCase();
                        applyFilter();
                    }
                });

        et_filter_input.setOnFocusChangeListener((v, gotFocus) ->

        {
            if (gotFocus)
                et_filter_input.setCompoundDrawables(null, null, null, null);
            else
                et_filter_input.setCompoundDrawablesWithIntrinsicBounds(0,
                        0,
                        R.drawable.search_on_background,
                        0);

        });
        et_filter_input.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
                S.hideKeyboard(BaseContactsActivity.this);
                return true;
            }
            return false;
        });
        bt_speak.setOnClickListener(this::onClick);
        bt_type.setOnClickListener(this::onClick);
        Toggeler.newBackgroundToggeler(bt_favorite, bt_favorite, new int[]{
                R.drawable.btn_selected, R.drawable.style_for_buttons
        }, new View.OnClickListener[]{
                v -> {
                    favorite = true;
                    if (baldTitleBar != null)
                        baldTitleBar.setGold(true);
                    applyFilter();
                },
                v -> {
                    favorite = false;
                    if (baldTitleBar != null)
                        baldTitleBar.setGold(false);
                    applyFilter();
                }
        });
    }

    private void displaySpeechRecognizer() {
        startActivityForResult(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        ),
                SPEECH_REQUEST_CODE);
    }

    protected abstract Cursor getCursorForFilter(String filter, boolean favorite);


    private void applyFilter() {
        if (contactRecyclerViewAdapter != null) {
            contactRecyclerViewAdapter.changeCursor(getCursorForFilter(filter, favorite));
        } else {
            contactRecyclerViewAdapter =
                    new ContactRecyclerViewAdapter(
                            BaseContactsActivity.this,
                            getCursorForFilter(filter, favorite),
                            recyclerView,
                            mode);
            recyclerView.setAdapter(contactRecyclerViewAdapter);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            final String spokenText =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            .get(0);
            et_filter_input.setText(spokenText);
            et_filter_input.setSelection(et_filter_input.getText().length());
        } else if (requestCode == SingleContactActivity.REQUEST_CHECK_CHANGE && resultCode == RESULT_OK) {
            applyFilter();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private static final String FILTER_STATE = "FILTER_STATE";
    private static final String FAVORITE_STATE = "FAVORITE_STATE";


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FILTER_STATE, filter);
        outState.putBoolean(FAVORITE_STATE, favorite);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filter = savedInstanceState.getString(FILTER_STATE);
        favorite = savedInstanceState.getBoolean(FAVORITE_STATE);
        et_filter_input.setText(filter);
        et_filter_input.setSelection(et_filter_input.getText().length());
        applyFilter();
    }


    private void onClick(View v) {
        if (v.getId() == R.id.bt_speak)
            displaySpeechRecognizer();
        favorite = false;
        if (baldTitleBar != null)
            baldTitleBar.setGold(false);
        applyFilter();
        bt_type.setVisibility(GONE);
        bt_favorite.setVisibility(GONE);
        et_filter_input.setVisibility(View.VISIBLE);
        et_filter_input.requestFocus();
        final InputMethodManager imm = (
                InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_filter_input, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_READ_CALL_LOG | PERMISSION_WRITE_CONTACTS | PERMISSION_CALL_PHONE | PERMISSION_READ_CONTACTS;
    }
}
