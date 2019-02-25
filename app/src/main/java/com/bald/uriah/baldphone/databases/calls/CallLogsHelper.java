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

import android.content.ContentResolver;

import com.bald.uriah.baldphone.databases.contacts.Contact;

import java.util.Collections;
import java.util.List;

/**
 * Simple Helper to get the call log.
 */
public class CallLogsHelper {
    public static List<Call> getAllCalls(ContentResolver contentResolver) {
        return Collections.emptyList();

//        try (Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, Call.PROJECTION, null, null, CallLog.Calls.DATE + " DESC")) {
//            final List<Call> calls = new ArrayList<>(cursor.getCount());
//            while (cursor.moveToNext()) {
//                calls.add(new Call(cursor));
//            }
//            return calls;
//        } catch (SecurityException e) {
//            throw new RuntimeException(e);
//        }
    }
//}

    public static List<Call> getForSpecificContact(ContentResolver contentResolver, Contact contact) {
        return Collections.emptyList();
//        final Uri contactUri = ContactsContract.Contacts.getLookupUri(contact.getId(), contact.getLookupKey());
//        try (Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, Call.PROJECTION, CallLog.Calls.CACHED_LOOKUP_URI + "=?", new String[]{contactUri.toString()}, CallLog.Calls.DATE + " DESC")) {
//            final List<Call> calls = new ArrayList<>(cursor.getCount());
//            while (cursor.moveToNext()) {
//                calls.add(new Call(cursor));
//            }
//            return calls;
//        } catch (SecurityException e) {
//            throw new RuntimeException(e);
//        }
    }
}
