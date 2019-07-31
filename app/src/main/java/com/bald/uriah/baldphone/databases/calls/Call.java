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

package com.bald.uriah.baldphone.databases.calls;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.databases.contacts.MiniContact;

public class Call {
    private static final String TAG = Call.class.getSimpleName();
    static final String[] PROJECTION = new String[]{
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.CACHED_LOOKUP_URI
    };
    public final String phoneNumber;
    public final int lengthInSeconds;
    public final long dateTime;
    public final int callType;
    public final String contactUri;

    public Call(String phoneNumber, int lengthInSeconds, long dateTime, int callType, String contactUri) {
        this.phoneNumber = phoneNumber;
        this.lengthInSeconds = lengthInSeconds;
        this.dateTime = dateTime;
        this.callType = callType;
        Log.e(TAG, "Call: contactUri=" + contactUri);
        this.contactUri = contactUri;
    }

    public Call(final Cursor cursor) {
        this(
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)),
                cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)),
                cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)),
                cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)),
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_LOOKUP_URI))
        );
    }

    @Nullable
    public MiniContact getMiniContact(Context context) {
        Cursor cursor = null;
        try {
            if (contactUri != null)
                cursor = context.getContentResolver().query(
                        Uri.parse(contactUri),
                        MiniContact.PROJECTION,
                        null,
                        null,
                        null);
            if (cursor == null || cursor.getCount() < 1) {
                if (phoneNumber != null)
                    cursor = context.getContentResolver().query(
                            Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                            MiniContact.PROJECTION,
                            null,
                            null,
                            null);
            }
            if (cursor == null || cursor.getCount() < 1) {
                return null;
            }
            cursor.moveToFirst();
            return new MiniContact(
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)),
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID)),
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1
            );
        } finally {
            if (cursor != null)
                cursor.close();

        }
    }

    @Override
    public String toString() {
        return "Call{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", lengthInSeconds=" + lengthInSeconds +
                ", dateTime=" + dateTime +
                ", callType=" + callType +
                ", contactUri='" + contactUri + '\'' +
                '}';
    }
}