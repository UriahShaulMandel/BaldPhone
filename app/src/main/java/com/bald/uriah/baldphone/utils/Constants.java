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

package com.bald.uriah.baldphone.utils;

import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;

/**
 * Constants that are used in multiple classes
 */
public class Constants {

    /**
     * contains the constants used in
     * {@link com.bald.uriah.baldphone.activities.media.SinglePhotoActivity}
     * and
     * {@link com.bald.uriah.baldphone.activities.media.PhotosActivity}
     */
    public interface PhotosConstants {
        String SORT_ORDER = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        String[] PROJECTION = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DATA, MediaStore.Images.Thumbnails.DATA,};
        Uri IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * contains the constants used in
     * {@link com.bald.uriah.baldphone.activities.media.SingleVideoActivity}
     * and
     * {@link com.bald.uriah.baldphone.activities.media.VideosActivity}
     */
    public interface VideosConstants {
        String SORT_ORDER = MediaStore.Video.Media.DATE_TAKEN + " DESC";
        String[] PROJECTION = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_TAKEN, MediaStore.Video.Media.DATA,};
        Uri VIDEOS_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * contains the constants used in
     * {@link com.bald.uriah.baldphone.databases.contacts.MiniContact}
     * <p>
     * and parent to {@link ContactConstants}
     */
    public interface BaseContactsConstants {
        String[] PROJECTION = new String[]{
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.STARRED,
        };

    }

    /**
     * contains the constants used in
     * {@link com.bald.uriah.baldphone.databases.contacts.Contact}
     */
    public interface ContactConstants extends BaseContactsConstants {
        int PHONE_TYPE_INDEX = 1, PHONE_NUMBER_INDEX = 2;
        String[] PHONE_PROJECTION = {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };
        String PHONE_SELECTION = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";

        int EMAIL_DATA_INDEX = 1;
        String[] EMAIL_PROJECTION = {
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
        };
        String EMAIL_SELECTION = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";

        int POBOX_INDEX = 3, STREET_INDEX = 4, CITY_INDEX = 5, REGION_INDEX = 6, POSTCODE_INDEX = 7, COUNTRY_INDEX = 8;

        String[] ADDRESS_PROJECTION = {
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE,

                ContactsContract.CommonDataKinds.StructuredPostal.POBOX,
                ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,

        };
        String ADDRESS_SELECTION = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

        String[] RAW_CONTACT_PROJECTION = new String[]{ContactsContract.RawContacts._ID};
        String RAW_CONTACT_SELECTION = ContactsContract.RawContacts.CONTACT_ID + " = ?";
    }
}
