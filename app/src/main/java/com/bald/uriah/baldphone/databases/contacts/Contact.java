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

package com.bald.uriah.baldphone.databases.contacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Pair;

import com.bald.uriah.baldphone.utils.D;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Immutable Contact class
 */
public class Contact {
    public static final String[] READ_CONTACT_PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.STARRED
    };
    private static final int PO_BOX = 0;
    private static final int STREET = 1;
    private static final int CITY = 2;
    private static final int STATE = 3;
    private static final int POSTAL_CODE = 4;
    private static final int COUNTRY = 5;
    private static final String[] PHONE_PROJECTION = {
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
    };
    private static final String PHONE_SELECTION = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
    private static final String[] EMAIL_PROJECTION = {
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID
    };
    private static final String EMAIL_SELECTION = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
    private static final String[] ADDRESS_PROJECTION = {
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.POBOX,
            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
            ContactsContract.CommonDataKinds.StructuredPostal.REGION,
            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
    };
    private static final String ADDRESS_SELECTION = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";


    private final int id;
    @NonNull
    private final String lookupKey;
    @NonNull
    private final List<Pair<Integer /*Type*/, String>> phoneList;
    @NonNull
    private final List<String> mailList;
    @NonNull
    private final List<Pair<Integer, String[]>> addressList;
    @NonNull
    private final List<String> whatsappNumbers;
    @NonNull
    private final String name;
    @Nullable
    private final String photo;
    private final boolean favorite;

    private Contact(int id,
                    @NonNull String lookupKey,
                    @NonNull List<Pair<Integer, String>> phoneList,
                    @NonNull List<String> mailList,
                    @NonNull List<Pair<Integer, String[]>> addressList,
                    @NonNull List<String> whatsappNumbers,
                    @NonNull String name,
                    @Nullable String photo,
                    boolean favorite) {
        this.id = id;
        this.lookupKey = lookupKey;
        this.phoneList = phoneList;
        this.mailList = mailList;
        this.addressList = addressList;
        this.whatsappNumbers = whatsappNumbers;
        this.name = name;
        this.photo = photo;
        this.favorite = favorite;
    }

    public static Contact fromId(@NonNull String id, @NonNull ContentResolver contentResolver) throws ContactNotFoundException {
        final Cursor contactsCursor =
                contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        READ_CONTACT_PROJECTION,
                        ContactsContract.Contacts._ID + " = ?",
                        new String[]{id},
                        null);
        if (!contactsCursor.moveToFirst()) throw new ContactNotFoundException();
        return readContact(contactsCursor, contentResolver);
    }

    public static Contact fromLookupKey(@NonNull String lookupKey, @NonNull ContentResolver contentResolver) throws ContactNotFoundException {
        final Cursor contactsCursor =
                contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        READ_CONTACT_PROJECTION,
                        ContactsContract.Contacts.LOOKUP_KEY + " = ?",
                        new String[]{lookupKey},
                        null);
        if (!contactsCursor.moveToFirst()) throw new ContactNotFoundException();
        return readContact(contactsCursor, contentResolver);
    }

    /**
     * @param cursor          cursor with the following projection: {{@link #READ_CONTACT_PROJECTION}}
     * @param contentResolver a content resolver
     * @return the contact
     */
    public static Contact readContact(Cursor cursor, ContentResolver contentResolver) {
        final String id;
        final String lookupKey;
        final List<Pair<Integer /*Type*/, String>> phoneList = new ArrayList<>();
        final List<String> mailList = new ArrayList<>();
        final List<Pair<Integer, String[]>> addressList = new ArrayList<>();
        final List<String> whatsappNumbers;
        final String name;
        final String photo;
        final boolean favorite;

        id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        photo = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

        try (Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION, PHONE_SELECTION, new String[]{id}, null)) {
            while (pCur.moveToNext()) {
                phoneList.add(new Pair<>(
                        pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),
                        pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                ));
            }
        }

        try (Cursor emailCur = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, EMAIL_PROJECTION, EMAIL_SELECTION, new String[]{id}, null)) {
            while (emailCur.moveToNext()) {
                mailList.add(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
            }
        }

        //Get Postal Address...
        try (Cursor addrCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                ADDRESS_PROJECTION,
                ADDRESS_SELECTION,
                new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE},
                null)) {
            while (addrCur.moveToNext()) {
                addressList.add(new Pair<>(addrCur.getInt(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)), new String[]{
                        addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)),
                        addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)),
                        addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)),
                        addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)),
                        addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)),
                        addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY))
                }));
            }
        }

        whatsappNumbers = getWhatsAppNumbers(contentResolver, name);
        favorite = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1;
        return new Contact(Integer.parseInt(id), lookupKey, phoneList, mailList, addressList, whatsappNumbers, name, photo, favorite);
    }

    private static List<String> getWhatsAppNumbers(ContentResolver contentResolver, String contactName) {
        final List<String> whatsappNumbers = new ArrayList<>();
        try (Cursor cursor1 = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.RawContacts._ID}, ContactsContract.RawContacts.ACCOUNT_TYPE + "= ? AND " + ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY + " = ?", new String[]{D.WHATSAPP_PACKAGE_NAME, contactName}, null)) {
            String rawContactId, phoneNumber;
            while (cursor1.moveToNext()) {
                rawContactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.RawContacts._ID));
                try (Cursor cursor2 = contentResolver.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Data.DATA3}, ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.RAW_CONTACT_ID + " = ? ", new String[]{"vnd.android.cursor.item/vnd.com.whatsapp.profile", rawContactId}, null)) {
                    while (cursor2.moveToNext()) {
                        phoneNumber = cursor2.getString(0);
                        if (TextUtils.isEmpty(phoneNumber))
                            continue;
                        if (phoneNumber.startsWith("Message"))
                            phoneNumber = phoneNumber.replace("Message", "");
                        whatsappNumbers.add(phoneNumber);
                    }
                }
            }
            return whatsappNumbers;
        }
    }

    @NonNull
    public List<Pair<Integer, String[]>> getAddressList() {
        return addressList;
    }

    @Nullable
    public CharSequence getAddress() {
        for (Pair<Integer, String[]> address :
                addressList) {
            final StringBuilder stringBuilder = new StringBuilder();
            if (address.second != null)
                for (String s :
                        address.second) {
                    if (s != null)
                        stringBuilder.append(s);
                }
            if (stringBuilder.length() != 0) {
                return stringBuilder;
            }
        }
        return null;
    }

    public String getMail() {
        return hasMail() ? mailList.get(0) : null;
    }

    @NonNull
    public List<String> getWhatsappNumbers() {
        return whatsappNumbers;
    }

    @NonNull
    public List<Pair<Integer, String>> getPhoneList() {
        return phoneList;
    }

    @Nullable
    public String getMobilePhone() {
        for (Pair<Integer, String> pair : phoneList)
            if (pair.first == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                return pair.second;
            }
        return null;
    }

    @Nullable
    public String getHomePhone() {
        for (Pair<Integer, String> pair : phoneList)
            if (pair.first == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                return pair.second;
            }
        return null;
    }

    public boolean hasPhone() {
        return phoneList.size() > 0;
    }

    public boolean hasWhatsapp() {
        return whatsappNumbers.size() > 0;
    }

    public boolean hasMail() {
        return mailList.size() > 0;
    }

    public boolean hasAddress() {
        return addressList.size() > 0;
    }

    public boolean hasPhoto() {
        return photo != null;
    }

    @NonNull
    public List<String> getMailList() {
        return mailList;
    }

    @NonNull
    public String getLookupKey() {
        return lookupKey;
    }

    public boolean isFavorite() {
        return favorite;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Nullable
    public String getPhoto() {
        return photo;
    }

    public static class ContactNotFoundException extends Exception {
        ContactNotFoundException() {
            super();
        }
    }
}