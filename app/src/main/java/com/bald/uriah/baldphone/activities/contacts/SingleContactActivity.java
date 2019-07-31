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

package com.bald.uriah.baldphone.activities.contacts;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.activities.SOSActivity;
import com.bald.uriah.baldphone.adapters.CallsRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.calls.Call;
import com.bald.uriah.baldphone.databases.calls.CallLogsHelper;
import com.bald.uriah.baldphone.databases.contacts.Contact;
import com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.utils.Toggeler;
import com.bald.uriah.baldphone.views.BaldLinearLayoutButton;
import com.bald.uriah.baldphone.views.BaldPictureTextButton;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.bald.uriah.baldphone.views.ScrollingHelper;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Simple Activity for interacting with a {@link Contact}.
 * {@link #CONTACT_ID} or {@link #CONTACT_LOOKUP_KEY} must be added
 */
public class SingleContactActivity extends BaldActivity {
    private static final String TAG = SingleContactActivity.class.getSimpleName();
    public static final String CONTACT_ID = "CONTACT_ID";
    public static final String CONTACT_LOOKUP_KEY = "CONTACT_KEY";
    public static final String PIC_URI_EXTRA = "PIC_URI_EXTRA";
    /**
     * request check if deleted contact, if returns {@link Activity#RESULT_OK} contact was deleted.
     */
    public static final int REQUEST_CHECK_CHANGE = 97;
    public static boolean newPictureAdded = false;
    private boolean viaId = false;
    private String contactKeyExtra;
    private BaldTitleBar baldTitleBar;
    private ImageView contact_image;
    private LinearLayout ll;
    private MediaPlayer mediaPlayer;
    private ContentResolver contentResolver;
    private LayoutInflater layoutInflater;
    private Contact contact; //final
    private boolean changed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_contact_activity);
        attachXml();
        contentResolver = getContentResolver();
        layoutInflater = getLayoutInflater();
        final Intent callingIntent = getIntent();
        viaId = callingIntent.hasExtra(CONTACT_ID);
        contactKeyExtra =
                viaId ?
                        callingIntent.getStringExtra(CONTACT_ID)
                        :
                        callingIntent.getStringExtra(CONTACT_LOOKUP_KEY)
        ;
        callingIntent.removeExtra(PIC_URI_EXTRA);

    }

    @Override
    protected void onStart() {
        super.onStart();

        final int childCount = ll.getChildCount();
        if (childCount > 2)
            ll.removeViews(2, ll.getChildCount() - 2);

        try {
            contact = viaId ?
                    Contact.fromId(contactKeyExtra, contentResolver) :
                    Contact.fromLookupKey(contactKeyExtra, contentResolver);
        } catch (Contact.ContactNotFoundException e) {
            finish();
            return;
        }

        baldTitleBar.setTitle(contact.getName());
        setupBar();

        if (contact.hasPhone())
            inflatePhones();

        if (contact.hasWhatsapp())
            inflateWhatsapp();

        if (contact.hasMail())
            inflateMails();

        if (contact.hasAddress())
            inflateAddresses();

        if (newPictureAdded)
            new Handler().postDelayed(() -> {
                newPictureAdded = false;
                String uri = null;
                try (final Cursor cursor =
                             contentResolver.query(
                                     ContactsContract.Contacts.CONTENT_URI,
                                     new String[]{
                                             ContactsContract.Contacts.PHOTO_URI},
                                     (viaId ?
                                             ContactsContract.Contacts._ID :
                                             ContactsContract.Contacts.LOOKUP_KEY)
                                             + " = ?",
                                     new String[]{contactKeyExtra},
                                     null)) {
                    if (cursor.moveToFirst())
                        uri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                }
                if (uri != null) {
                    loadPhoto(uri);
                } else
                    contact_image.setVisibility(View.GONE);

            }, 1000);

        else {
            if (contact.hasPhoto())
                loadPhoto(contact.getPhoto());
            else
                contact_image.setVisibility(View.GONE);
        }

        inflateAdders();

        final List<Call> callList =
                CallLogsHelper.getForSpecificContact(contentResolver, contact);
        if (!callList.isEmpty())
            inflateHistory(callList);

    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onStop();

    }

    private void attachXml() {
        baldTitleBar = findViewById(R.id.bald_title_bar);
        ll = findViewById(R.id.ll_info);
        contact_image = findViewById(R.id.contact_image);
    }

    private void inflatePhones() {
        final List<Pair<Integer, String>> phoneList = contact.getPhoneList(); //removing dup
        for (int i = 0, phoneListSize = phoneList.size(); i < phoneListSize - 1; i++) {
            final Pair<Integer, String> pair = phoneList.get(i);
            final Pair<Integer, String> next = phoneList.get(i + 1);
            if (pair.second
                    .replaceAll("[^0123456789]", "")
                    .equals(next.second
                            .replaceAll("[^0123456789]", ""))) {
                phoneList.remove(pair);
                phoneListSize--;
                i--;
            }
        }

        for (final Pair<Integer /*Type*/ , String> pair : contact.getPhoneList()) {
            View layout = layoutInflater.inflate(R.layout.contact_number, ll, false);
            final TextView tv_type = layout.findViewById(R.id.tv_type);
            final TextView tv_value = layout.findViewById(R.id.tv_value);
            final BaldLinearLayoutButton call = layout.findViewById(R.id.call);
            final BaldLinearLayoutButton message = layout.findViewById(R.id.message);

            tv_type.setText(ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    getResources(),
                    pair.first,
                    "Custom"));
            tv_value.setText(pair.second);
            call.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + pair.second));
                try {
                    startActivity(intent);
                } catch (SecurityException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            });
            message.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_SENDTO)
                    .setData(Uri.parse("smsto:" + Uri.encode(pair.second)))));

            ll.addView(layout);
        }

    }

    private void inflateWhatsapp() {
        final List<String> whatappNumbers = contact.getWhatsappNumbers();
        for (String whatappNumber : whatappNumbers) {
            final View layout = layoutInflater.inflate(R.layout.contact_whatsapp, ll, false);
            layout.findViewById(R.id.whatsapp).setOnClickListener((v) ->
                    startActivity(
                            new Intent("android.intent.activate.MAIN")
                                    .setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"))
                                    .putExtra("jid",
                                            PhoneNumberUtils.stripSeparators(
                                                    whatappNumber
                                                            .replaceAll(
                                                                    "[^0123456789]",
                                                                    "")
                                            ) + "@s.whatsapp.net")
                    )
            );
            ll.addView(layout);
        }
    }

    private void inflateMails() {
        final List<String> mailList = contact.getMailList();
        for (int i = 0, mailListSize = mailList.size(); i < mailListSize - 1; i++) {
            final String phone = mailList.get(i);
            final String next = mailList.get(i + 1);
            if (phone.equals(next)) {
                mailList.remove(i);
                mailListSize--;
                i--;
            }
        }
        for (final String phone : contact.getMailList()) {
            final View layout = layoutInflater.inflate(R.layout.contact_mail, ll, false);
            final TextView tv_value = layout.findViewById(R.id.tv_value);
            final BaldLinearLayoutButton mail = layout.findViewById(R.id.mail);
            tv_value.setText(phone);
            mail.setOnClickListener(v -> {
                final Intent intent = new Intent(Intent.ACTION_SEND)
                        .setType("text/html")
                        .putExtra(Intent.EXTRA_EMAIL, phone);
                startActivity(intent);

            });
            ll.addView(layout);
        }

    }

    private void inflateAdders() {
        final View view = layoutInflater.inflate(R.layout.contact_add_to, ll, false);
        final BaldPictureTextButton
                home = view.findViewById(R.id.home),
                favorite = view.findViewById(R.id.favorite),
                sos = view.findViewById(R.id.sos);

        Toggeler.newTextImageToggeler(
                home,
                home.getImageView(),
                home.getTextView(),
                new int[]{R.drawable.home_on_button, R.drawable.remove_on_button},
                new int[]{R.string.add_to_home, R.string.remove_from_home},
                new View.OnClickListener[]{
                        (v) -> HomeScreenPinHelper.removeContact(v.getContext(), contact.getLookupKey()),
                        (v) -> HomeScreenPinHelper.pinContact(v.getContext(), contact.getLookupKey())
                },
                HomeScreenPinHelper.isPinned(this, contact.getLookupKey()) ? 1 : 0
        );

        Toggeler.newTextImageToggeler(
                sos,
                sos.getImageView(),
                sos.getTextView(),
                new int[]{R.drawable.emergency, R.drawable.remove_on_button},
                new int[]{R.string.add_to_sos, R.string.remove_from_sos},
                new View.OnClickListener[]{
                        (v) -> SOSActivity.PinHelper.removeContact(v.getContext(), contact.getLookupKey()),
                        (v) -> {
                            if (!SOSActivity.PinHelper.pinContact(v.getContext(), contact.getLookupKey())) {
                                BDB.from(v.getContext())
                                        .setTitle(R.string.sos_is_full)
                                        .setSubText(R.string.sos_is_full_subtext)
                                        .addFlag(BDialog.FLAG_OK)
                                        .show();
                                v.setVisibility(View.GONE);
                            }
                        }
                },
                SOSActivity.PinHelper.isPinned(this, contact.getLookupKey()) ? 1 : 0
        );

        final ContentValues STAR = new ContentValues();
        STAR.put(ContactsContract.Contacts.STARRED, 1);
        final ContentValues UNSTAR = new ContentValues();
        UNSTAR.put(ContactsContract.Contacts.STARRED, 0);
        baldTitleBar.setGold(contact.isFavorite());
        Toggeler.newTextImageToggeler(
                favorite,
                favorite.getImageView(),
                favorite.getTextView(),
                new int[]{R.drawable.star_on_button, R.drawable.star_remove_on_button},
                new int[]{R.string.add_to_favorite, R.string.remove_from_favorite},
                new View.OnClickListener[]{
                        (v) -> {
                            contentResolver.update(
                                    ContactsContract.Contacts.CONTENT_URI,
                                    UNSTAR,
                                    ContactsContract.Contacts.LOOKUP_KEY + "=?",
                                    new String[]{contact.getLookupKey()});
                            baldTitleBar.setGold(false);
                            changed = true;
                        },
                        (v) -> {
                            contentResolver.update(
                                    ContactsContract.Contacts.CONTENT_URI,
                                    STAR,
                                    ContactsContract.Contacts.LOOKUP_KEY + "=?",
                                    new String[]{contact.getLookupKey()});
                            baldTitleBar.setGold(true);
                            changed = true;
                        }
                },
                contact.isFavorite() ? 1 : 0

        );
        ll.addView(view);
    }

    private void inflateAddresses() {
        for (Pair<Integer, String[]> pair : contact.getAddressList()) {
            final View layout = layoutInflater.inflate(R.layout.contact_address, ll, false);
            final TextView tv_addressType = layout.findViewById(R.id.tv_address);
            final BaldPictureTextButton button = layout.findViewById(R.id.address_button);
            tv_addressType.setText(
                    String.format("%s %s",
                            String.valueOf(ContactsContract.CommonDataKinds.SipAddress.getTypeLabel(getResources(),
                                    pair.first,
                                    getText(R.string.custom)))
                            , String.valueOf(getText(R.string.address)
                            )
                    )
            );

            final StringBuilder stringBuilder = new StringBuilder();
            for (final String s : pair.second) {
                if (s == null || s.equals(""))
                    continue;

                stringBuilder.append(s).append(" ");

            }
            button.setText(stringBuilder);
            button.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + stringBuilder))));

            ll.addView(layout);
        }
    }

    private void inflateHistory(List<Call> callList) {
        final View view = layoutInflater.inflate(R.layout.contact_history, ll, false);
        final ScrollingHelper scrollingHelper = view.findViewById(R.id.scrolling_helper);
        final RecyclerView recyclerView = scrollingHelper.findViewById(R.id.child);
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.ll_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(new CallsRecyclerViewAdapter(callList, this));

        final BaldPictureTextButton show = view.findViewById(R.id.bt_show);
        Toggeler.newSimpleTextImageToggeler(
                show,
                show.getImageView(),
                show.getTextView(),
                R.drawable.drop_down_on_button,
                R.drawable.drop_up_on_button,
                R.string.show,
                R.string.hide,
                v -> scrollingHelper.setVisibility(View.VISIBLE),
                v -> scrollingHelper.setVisibility(View.GONE));
        ll.addView(view);
    }

    private void setupBar() {
        final Intent editIntent =
                new Intent(this, AddContactActivity.class)
                        .putExtra(CONTACT_LOOKUP_KEY, contact.getLookupKey());
        final View.OnClickListener deleteListener = (v) ->
                BDB.from(this)
                        .addFlag(BDialog.FLAG_YES | BDialog.FLAG_CANCEL)
                        .setSubText(String.format(getString(R.string.are_you_sure_you_want_to_delete___), contact.getName()))
                        .setPositiveButtonListener(params -> {
                            deleteContact();
                            return true;
                        })
                        .show();
        findViewById(R.id.bt_delete).setOnClickListener(deleteListener);
        findViewById(R.id.bt_edit).setOnClickListener(v -> v.getContext().startActivity(editIntent));

        final Uri vcardUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, contact.getLookupKey());
        final Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType(ContactsContract.Contacts.CONTENT_VCARD_TYPE)
                .putExtra(Intent.EXTRA_STREAM, vcardUri)
                .putExtra(Intent.EXTRA_SUBJECT, contact.getName());

        findViewById(R.id.bt_share).setOnClickListener(v -> {
                    changed = true;
                    S.share(this, shareIntent);
                }
        );

    }

    private void deleteContact() {
        contentResolver.delete(
                Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, contact.getLookupKey()),
                null,
                null);
        changed = true;
        finish();
    }

    @Override
    public void finish() {
        if (changed) setResult(RESULT_OK);
        super.finish();
    }

    private void loadPhoto(String uriToLoad) {
        if (S.isValidContextForGlide(contact_image.getContext()))
            Glide.with(contact_image).load(uriToLoad).into(contact_image);
        final int width = ((ViewGroup) contact_image.getParent()).getWidth();
        contact_image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width));
        contact_image.setScaleType(ImageView.ScaleType.FIT_XY);
        ll.setMinimumHeight(width * 3); //TODO
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            BaldToast.error(this);
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_WRITE_CONTACTS | PERMISSION_READ_CONTACTS | PERMISSION_CALL_PHONE | PERMISSION_READ_CALL_LOG;
    }
}
