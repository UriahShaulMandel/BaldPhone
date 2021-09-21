package com.bald.uriah.baldphone.apps.contacts;

import android.provider.ContactsContract;

/**
 * contains the constants used in
 * {@link Contact}
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
