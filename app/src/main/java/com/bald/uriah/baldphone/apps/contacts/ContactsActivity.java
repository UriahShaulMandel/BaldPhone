/*
 * Copyright 2019 Uriah Shaul Mandel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bald.uriah.baldphone.apps.contacts;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.ContactRecyclerViewAdapter;

/**
 * Activity for viewing and adding {@link com.bald.uriah.baldphone.databases.contacts.Contact}.
 * most of the activity actually defind in {@link BaseContactsActivity}
 */
public class ContactsActivity extends BaseContactsActivity {
    private static final String TAG = ContactsActivity.class.getSimpleName();
    private static final String STAR_SELECTION = ContactsContract.Data.STARRED + " = 1";
    private static final String SORT_ORDER = "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC";
    private View add_contact;

    @Override
    protected int layout() {
        return R.layout.contacts_activity;
    }

    @Override
    protected void viewsInit() {
        super.viewsInit();
        add_contact.setOnClickListener(v -> startActivity(new Intent(this, AddContactActivity.class)));
    }

    @Override
    protected void attachXml() {
        super.attachXml();
        add_contact = findViewById(R.id.bt_add_contact);
    }

    @Override
    protected Cursor getCursorForFilter(String filter, boolean favorite) {
        if (!TextUtils.isEmpty(filter) && TextUtils.isDigitsOnly(filter)) {
            return getContactsByNumberFilter(filter, favorite);
        } else {
            return getContactsByNameFilter(filter, favorite);
        }
    }

    private Cursor getContactsByNameFilter(String filter, boolean favorite) {
        final String selection =
                String.format("%s LIKE ?%s",
                        ContactsContract.Data.DISPLAY_NAME,
                        favorite ? (" AND " + STAR_SELECTION) : ""
                );
        final String[] args = {"%" + filter + "%"};
        try {
            return contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                    ContactRecyclerViewAdapter.PROJECTION,
                    selection,
                    args,
                    SORT_ORDER);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private Cursor getContactsByNumberFilter(String filter, boolean favorite) {
        final Uri uri =
                filter.equals("") ?
                        ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI :
                        Uri.withAppendedPath(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                                Uri.encode(filter)
                        );
        try {
            return contentResolver.query(
                    uri,
                    ContactRecyclerViewAdapter.PROJECTION,
                    favorite ? STAR_SELECTION : null,
                    null,
                    SORT_ORDER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}