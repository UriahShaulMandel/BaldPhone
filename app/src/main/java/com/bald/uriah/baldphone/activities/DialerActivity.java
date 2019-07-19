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

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.contacts.AddContactActivity;
import com.bald.uriah.baldphone.adapters.ContactRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.contacts.Contact;
import com.bald.uriah.baldphone.databases.contacts.MiniContact;
import com.bald.uriah.baldphone.utils.BaldToast;

public class DialerActivity extends BaldActivity {
    public static final String SORT_ORDER =
            "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC";
    private static final String TAG = DialerActivity.class.getSimpleName();
    private final static String[] PROJECTION =
            {ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data._ID, ContactsContract.Contacts.PHOTO_URI, ContactsContract.Data.LOOKUP_KEY, ContactsContract.Data.STARRED};
    private static final String NUMBER_STATE = "NUMBER_STATE";

    private ContentResolver contentResolver;
    private ContactRecyclerViewAdapter contactRecyclerViewAdapter;
    private RecyclerView recyclerView;
    private TextView tv_number;
    private View b_call, b_clear, b_hash, b_sulamit, b_backspace, empty_view;
    private View[] numpad;
    private StringBuilder number = new StringBuilder();

    public static void call(final CharSequence number, final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                context.startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + number)));
            } catch (SecurityException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else
            BaldToast.from(context) // should NEVER occur, but in case..
                    .setType(BaldToast.TYPE_ERROR)
                    .setText(R.string.phone_ask_permission_subtext)
                    .show();

    }

    public static void call(final MiniContact miniContact, final Context context) {
        try {
            call(Contact.fromLookupKey(miniContact.lookupKey, context.getContentResolver()).getPhoneList().get(0).second, context);
        } catch (Exception e) {
            BaldToast.error(context);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        setContentView(R.layout.dialer);
        contentResolver = getContentResolver();

        attachXml();
        setOnClickListeners();
        setupYoutube(2);
        searchForContact();
    }

    private void getContactsByNumberFilter() {
        final Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(number.toString()));
        final Cursor contactsCursor = contentResolver.query(uri, PROJECTION, null, null, SORT_ORDER);
        if (contactRecyclerViewAdapter != null) {
            contactRecyclerViewAdapter.changeCursor(contactsCursor);
        } else {
            contactRecyclerViewAdapter = new ContactRecyclerViewAdapter(this, contactsCursor, recyclerView, ContactRecyclerViewAdapter.MODE_DEFAULT);
            recyclerView.setAdapter(contactRecyclerViewAdapter);
        }
    }

    private void attachXml() {
        recyclerView = findViewById(R.id.contacts_recycler_view);
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.ll_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        tv_number = findViewById(R.id.tv_number);
        numpad = new View[]{
                findViewById(R.id.b_0),
                findViewById(R.id.b_1),
                findViewById(R.id.b_2),
                findViewById(R.id.b_3),
                findViewById(R.id.b_4),
                findViewById(R.id.b_5),
                findViewById(R.id.b_6),
                findViewById(R.id.b_7),
                findViewById(R.id.b_8),
                findViewById(R.id.b_9)
        };
        empty_view = findViewById(R.id.empty_view);
        b_call = findViewById(R.id.b_call);
        b_clear = findViewById(R.id.b_clear);
        b_backspace = findViewById(R.id.b_backspace);
        b_sulamit = findViewById(R.id.b_sulamit);
        b_hash = findViewById(R.id.b_hash);
    }

    private void setOnClickListeners() {
        for (char i = '0'; i <= '9'; i++)
            numpad[i - '0'].setOnClickListener(new DialerClickListener(i));
        b_sulamit.setOnClickListener(new DialerClickListener('*'));
        b_hash.setOnClickListener(new DialerClickListener('#'));

        b_call.setOnClickListener(v -> call(number, this));
        b_clear.setOnClickListener(v -> {
            number.setLength(0);
            tv_number.setText(number);
            searchForContact();
        });
        b_backspace.setOnClickListener(v -> {
            number.setLength(number.length() > 1 ? number.length() - 1 : 0);
            tv_number.setText(number);
            searchForContact();
        });
        empty_view.setOnClickListener(v -> {
            startActivity(new Intent(this, AddContactActivity.class)
                    .putExtra(AddContactActivity.CONTACT_NUMBER, (CharSequence) number));
        });

    }

    private void searchForContact() {
        getContactsByNumberFilter();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(NUMBER_STATE, number);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final CharSequence charSequence = savedInstanceState.getCharSequence(NUMBER_STATE);
        if (charSequence != null) {
            number = new StringBuilder(charSequence);
            tv_number.setText(number);
            searchForContact();
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_READ_CONTACTS | PERMISSION_CALL_PHONE;
    }

    private class DialerClickListener implements View.OnClickListener {
        private final char c;

        DialerClickListener(final char c) {
            this.c = c;
        }

        @Override
        public void onClick(final View v) {
            number.append(c);
            tv_number.setText(number);
            searchForContact();
        }
    }
}