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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.contacts.ContactsActivity;
import com.bald.uriah.baldphone.adapters.ContactRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.contacts.MiniContact;
import com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.views.BaldLinearLayoutButton;

import java.util.*;

import static com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper.SHARED_PREFS_KEY;

public class SOSActivity extends BaldActivity {
    private BaldLinearLayoutButton ec1, ec2, ecReal;

    private static void setupEC(BaldLinearLayoutButton baldLinearLayoutButton, MiniContact miniContact) {
        if (miniContact.photo != null)
            ((ImageView) baldLinearLayoutButton.getChildAt(0)).setImageURI(Uri.parse(miniContact.photo));
        else
            ((ImageView) baldLinearLayoutButton.getChildAt(0)).setImageResource(R.drawable.face_on_button);

        ((TextView) baldLinearLayoutButton.getChildAt(1)).setText(miniContact.name);
        baldLinearLayoutButton.setOnClickListener(v -> DialerActivity.call(miniContact, v.getContext()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        setContentView(R.layout.activity_sos);
        setupXml();
        setupYoutube(2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final View.OnClickListener addContactListener = v -> startActivity(new Intent(v.getContext().getApplicationContext(), ContactsActivity.class).putExtra(ContactsActivity.INTENT_EXTRA_CONTACT_ADAPTER_MODE, ContactRecyclerViewAdapter.MODE_SOS));
        final List<MiniContact> miniContacts = PinHelper.getAllPinnedContacts(this);
        if (miniContacts != null) {
            final int size = miniContacts.size();
            if (size > 0)
                setupEC(ec1, miniContacts.get(0));
            else
                ec1.setOnClickListener(addContactListener);

            if (size > 1)
                setupEC(ec2, miniContacts.get(1));
            else
                ec2.setOnClickListener(addContactListener);
        } else {
            ec1.setOnClickListener(addContactListener);
            ec2.setOnClickListener(addContactListener);
        }

        ecReal.setOnClickListener((v) -> {
            BDB.from(SOSActivity.this)
                    .setTitle("")
                    .setSubText(v.getContext().getString(R.string.are_you_sure_you_want_to_call_emergency_services))
                    .setCancelable(true)
                    .setPositiveButtonListener(params -> {
                        callEmergencyNumber();
                        return true;
                    }).show();
        });
    }

    private void callEmergencyNumber() {
        DialerActivity.call("112", this);//should work 99.99% of the times
    }

    private void setupXml() {
        ec1 = findViewById(R.id.ec1);
        ec2 = findViewById(R.id.ec2);
        ecReal = findViewById(R.id.ec_real);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_up);
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_CALL_PHONE | PERMISSION_READ_CONTACTS;
    }

    public static class PinHelper {
        /**
         * @param context
         * @param lookupKey
         * @return true if succeeded
         */
        public static boolean pinContact(Context context, String lookupKey) {
            final SharedPreferences sharedPreferences = context.getSharedPreferences(HomeScreenPinHelper.PinnedContactPreferences.KEY, Context.MODE_PRIVATE);
            final Set<String> befSet = sharedPreferences.getStringSet(HomeScreenPinHelper.PinnedContactPreferences.SOS_KEY, null);
            final Set<String> newSet;
            if (befSet == null)
                newSet = new HashSet<>();
            else {
                if (befSet.size() > 1)
                    return false;
                else
                    newSet = new HashSet<>(befSet);
            }
            newSet.add(lookupKey);
            sharedPreferences.edit().putStringSet(HomeScreenPinHelper.PinnedContactPreferences.SOS_KEY, newSet).apply();
            return true;
        }

        public static boolean isPinned(Context context, String lookupKey) {
            Set<String> set = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).getStringSet(HomeScreenPinHelper.PinnedContactPreferences.SOS_KEY, null);
            return set != null && set.contains(lookupKey);
        }

        public static void removeContact(Context context, String lookupKey) {
            final SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            final Set<String> befSet = sharedPreferences.getStringSet(HomeScreenPinHelper.PinnedContactPreferences.SOS_KEY, null);
            final Set<String> newSet;
            if (befSet == null)
                newSet = new HashSet<>();
            else
                newSet = new HashSet<>(befSet);
            newSet.remove(lookupKey);
            sharedPreferences.edit().putStringSet(HomeScreenPinHelper.PinnedContactPreferences.SOS_KEY, newSet).apply();
        }

        /**
         * @param context
         * @return sorted list of lookup keys
         */
        private static List<MiniContact> getAllPinnedContacts(Context context) {
            final Set<String> lookupKeys = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).getStringSet(HomeScreenPinHelper.PinnedContactPreferences.SOS_KEY, null);
            if (lookupKeys == null)
                return null;
            final List<MiniContact> ret = new ArrayList<>(lookupKeys.size());
            final ContentResolver contentResolver = context.getContentResolver();
            for (String lookupKey :
                    lookupKeys) {
                try (Cursor cursor = contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        MiniContact.PROJECTION,
                        ContactsContract.Data.LOOKUP_KEY + " = ?",
                        new String[]{
                                lookupKey
                        }, null)) {
                    if (cursor.moveToFirst()) {
                        ret.add(new MiniContact(
                                lookupKey,
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)),
                                cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID)),
                                cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1
                        ));
                    } else {
                        removeContact(context, lookupKey);
                    }

                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
            Collections.sort(ret, (o1, o2) -> o1.name.compareTo(o2.name));
            return ret;
        }
    }
}