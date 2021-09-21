package com.bald.uriah.baldphone.apps.contacts;

import android.provider.ContactsContract;

/**
 * contains the constants used in
 * {@link MiniContact}
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
