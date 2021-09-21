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

package com.bald.uriah.baldphone.apps.recent_calls;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.apps.contacts.MiniContact;

public class Call {
    private static final String TAG = Call.class.getSimpleName();
    public static final String[] PRIVATE_NUMBERS = {"-1", "-2"};
    static final String[] PROJECTION = new String[]{
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.CACHED_LOOKUP_URI,
//            CallLog.Calls.NEW, Currently it's commented because new calls are not marked in any way
//            CallLog.Calls.IS_READ,
    };
    public final String phoneNumber;
    public final int lengthInSeconds;
    public final long dateTime;
    public final int callType;
    public final String contactUri;
//    public final boolean neW; Currently it's commented because new calls are not marked in any way

    public Call(String phoneNumber, int lengthInSeconds, long dateTime, int callType, String contactUri/*, boolean neW*/) {
        this.phoneNumber = phoneNumber;
        this.lengthInSeconds = lengthInSeconds;
        this.dateTime = dateTime;
        this.callType = callType;
        this.contactUri = contactUri;
//        this.neW = neW;
    }

    public Call(final Cursor cursor) {
        this(
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)),
                cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)),
                cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)),
                cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)),
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_LOOKUP_URI))
//                ,(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW)) == 1) && (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.IS_READ)) == 0)
        );
    }

    @Nullable
    public MiniContact getMiniContact(Context context) {
        if (isPrivate())
            return null;
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

    public boolean isPrivate() {
        for (final String privateNumber : PRIVATE_NUMBERS) {
            if (privateNumber.equals(phoneNumber))
                return true;
        }
        return false;
    }
}