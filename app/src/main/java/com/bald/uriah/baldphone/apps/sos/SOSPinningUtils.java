package com.bald.uriah.baldphone.apps.sos;

import static com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper.SHARED_PREFS_KEY;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.bald.uriah.baldphone.databases.contacts.MiniContact;
import com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SOSPinningUtils {
    /**
     * @return true if succeeded
     */
    public static boolean pinContact(Context context, String lookupKey) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(HomeScreenPinHelper.PinnedContactPreferences.KEY, Context.MODE_PRIVATE);
        final Set<String> befSet = sharedPreferences.getStringSet(HomeScreenPinHelper.PinnedContactPreferences.SOS_KEY, null);
        final Set<String> newSet;
        if (befSet == null)
            newSet = new HashSet<>();
        else {
            if (befSet.size() >= SOSActivity.MAX_PINNED_CONTACTS)
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
    static List<MiniContact> getAllPinnedContacts(Context context) {
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
