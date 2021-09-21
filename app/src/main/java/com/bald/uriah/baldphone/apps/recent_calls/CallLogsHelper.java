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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.bald.uriah.baldphone.BuildConfig;
import com.bald.uriah.baldphone.adapters.CallsRecyclerViewAdapter;
import com.bald.uriah.baldphone.apps.contacts.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.provider.CallLog.Calls.IS_READ;
import static android.provider.CallLog.Calls.NEW;
import static android.provider.CallLog.Calls.TYPE;

/**
 * Simple Helper to get the call log.
 */
public class CallLogsHelper {
    public static List<Call> getAllCalls(ContentResolver contentResolver) {
        try (Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, Call.PROJECTION, null, null, CallLog.Calls.DATE + " DESC")) {
            final List<Call> calls = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                calls.add(new Call(cursor));
            }
            return calls;
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Call> getForSpecificContact(ContentResolver contentResolver, Contact contact) {
        if (BuildConfig.FLAVOR.equals("gPlay"))
            return new ArrayList<>();
        final Uri contactUri = ContactsContract.Contacts.getLookupUri(contact.getId(), contact.getLookupKey());
        try (Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, Call.PROJECTION, CallLog.Calls.CACHED_LOOKUP_URI + "=?", new String[]{contactUri.toString()}, CallLog.Calls.DATE + " DESC")) {
            final List<Call> calls = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                calls.add(new Call(cursor));
            }
            return calls;
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void markAllAsRead(ContentResolver contentResolver) {
        final ContentValues values = new ContentValues();
        values.put(IS_READ, true);
        values.put(CallLog.Calls.NEW, false);
        try {
            contentResolver.update(CallLog.Calls.CONTENT_URI, values, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAllReadSafe(ContentResolver contentResolver) {
        try (final Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, new String[]{TYPE, IS_READ, NEW}, String.format(Locale.US, "%s=0 AND %s=1 AND %s=%d", IS_READ, NEW, TYPE, CallsRecyclerViewAdapter.MISSED_TYPE), null, null)) {
            return cursor.getCount() == 0;
        } catch (SecurityException ignore) {
            return true;
        }
    }
}
