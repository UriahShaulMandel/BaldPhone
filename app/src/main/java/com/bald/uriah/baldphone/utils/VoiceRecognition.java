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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.DialerActivity;
import com.bald.uriah.baldphone.activities.contacts.SingleContactActivity;
import com.bald.uriah.baldphone.databases.apps.App;
import com.bald.uriah.baldphone.databases.apps.AppsDatabase;
import com.bald.uriah.baldphone.databases.contacts.Contact;

import java.util.List;


public class VoiceRecognition {

    public enum AnswerType {
        FAILED {
            @Override
            public String submit(Context context, Object object) {
                return (String) object;
            }
        },
        CALL {
            @Override
            public String submit(Context context, Object object) {
                DialerActivity.call((CharSequence) object, context);
                return String.format(context.getString(R.string.calling__), object);
            }
        },
        OPEN_CONTACT {
            @Override
            public String submit(Context context, Object object) {
                final Contact contact = (Contact) object;
                context.startActivity(new Intent(context, SingleContactActivity.class).putExtra(SingleContactActivity.CONTACT_LOOKUP_KEY, contact.getLookupKey()));
                return String.format(context.getString(R.string.opening___), contact.getName());

            }
        },
        OPEN {
            @Override
            public String submit(Context context, Object object) {
                context.startActivity(
                        new Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_LAUNCHER)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                                .setComponent(
                                        ComponentName.unflattenFromString(((App) object).getFlattenComponentName())
                                )
                );

                return String.format(context.getString(R.string.opening___), ((App) object).getLabel());
            }
        };

        public abstract String submit(Context context, Object object);
    }


    public static class VoiceRecognitionException extends Exception {
        public VoiceRecognitionException(String message) {
            super(message);
        }
    }

    public static class Answer {
        final AnswerType type;
        final Object object;

        public Answer(AnswerType type, Object object) {
            this.type = type;
            this.object = object;
        }

        public String submit(Context context) {
            return type.submit(context, object);
        }
    }


    private static final String TAG = VoiceRecognition.class.getSimpleName();


    //This method is indeed hard coded and problematic.
    //BUT
    //Until making more commands, which will be added in the future,
    //It is the fastest way to do it.
    public static Answer recognizeVoice(final Context context, final String input) {
        final VoiceRecognitionValues voiceRecognitionValues = VoiceRecognitionValues.getInstance(context);
        final String[] arr = input.split(" ");

        if (arr.length >= 1) {
            int whichWordIsCall = -1;
            outer_loop:
            for (int i = 0; i < arr.length; i++)
                for (int j = 0; j < voiceRecognitionValues.call.length; j++)
                    if (arr[i].equalsIgnoreCase(voiceRecognitionValues.call[j])) {
                        whichWordIsCall = i;
                        break outer_loop;
                    }
            final StringBuilder stringBuilder = new StringBuilder();
            if (context.getString(R.string.hebrew).equals("1")) {
                if (whichWordIsCall != -1)
                    if (arr.length > whichWordIsCall + 1) {
                        if (arr[whichWordIsCall + 1].startsWith("ל")) {
                            arr[whichWordIsCall + 1] = arr[whichWordIsCall + 1].substring(1);
                        }
                    }
            }
            for (int i = whichWordIsCall + 1; i < arr.length; i++) {
                stringBuilder.append(arr[i]);
                stringBuilder.append(" ");
            }
            final int stringBuilderLength = stringBuilder.length();
            stringBuilder.setLength(stringBuilderLength > 1 ? stringBuilderLength - 1 : 0);
            try {
                final Contact contact = getContactByNameFilter(stringBuilder.toString(), context);
                if (whichWordIsCall != -1) {
                    if (contact.hasPhone())
                        return new Answer(AnswerType.CALL, contact.getPhoneList().get(0).second);
                } else {
                    return new Answer(AnswerType.OPEN_CONTACT, contact);
                }
            } catch (VoiceRecognitionException e) {
                if (whichWordIsCall != -1)
                    return new Answer(AnswerType.FAILED, e.getMessage());
            }
        }
        {
            int whichWordIsOpen;
            if (arr.length == 1) {
                whichWordIsOpen = -1;
            } else {
                whichWordIsOpen = -2;
                outer_loop:
                for (int i = 0; i < arr.length; i++)
                    for (int j = 0; j < voiceRecognitionValues.open.length; j++)
                        if (arr[i].equalsIgnoreCase(voiceRecognitionValues.open[j])) {
                            whichWordIsOpen = i;
                            break outer_loop;
                        }
            }
            if (whichWordIsOpen != -2) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = whichWordIsOpen + 1; i < arr.length; i++) {
                    stringBuilder.append(arr[i]);
                    stringBuilder.append(" ");
                }
                final int stringBuilderLength = stringBuilder.length();
                stringBuilder.setLength(stringBuilderLength > 1 ? stringBuilderLength - 1 : 0);
                List<App> appList = AppsDatabase.getInstance(context).appsDatabaseDao().getAllLike(stringBuilder.toString());
                if (appList.size() > 2)
                    return new Answer(AnswerType.FAILED, String.format(context.getString(R.string.too_many_apps_with___in_them), stringBuilder.toString()));
                if (appList.size() == 0)
                    return new Answer(AnswerType.FAILED, String.format(context.getString(R.string.___app_was_not_found), stringBuilder.toString()));
                return new Answer(AnswerType.OPEN, appList.get(0));
            }
        }

        return new Answer(AnswerType.FAILED, context.getString(R.string.please_repeat_that));
    }

    private static final String SELECTION = ContactsContract.Data.DISPLAY_NAME + " LIKE ? ";

    @NonNull
    private static Contact getContactByNameFilter(final String filter, final Context context) throws VoiceRecognitionException {
        final String[] args = {
                "%" + filter + "%"
        };
        final Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                Contact.READ_CONTACT_PROJECTION,
                SELECTION,
                args,
                null);
        if (cursor.getCount() > 2)
            throw new VoiceRecognitionException(String.format(context.getString(R.string.too_many_contacts_with___in_them), filter));
        if (cursor.moveToFirst()) {
            return Contact.readContact(cursor, context.getContentResolver());
        } else {
            throw new VoiceRecognitionException(String.format(context.getString(R.string.no_contact_contains___), filter));
        }
    }
}