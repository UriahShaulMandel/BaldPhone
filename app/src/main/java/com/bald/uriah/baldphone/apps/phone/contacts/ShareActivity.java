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

package com.bald.uriah.baldphone.apps.phone.contacts;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.ContactRecyclerViewAdapter;
import com.bald.uriah.baldphone.adapters.IntentAdapter;
import com.bald.uriah.baldphone.databases.contacts.Contact;
import com.bald.uriah.baldphone.core.BaldToast;
import com.bald.uriah.baldphone.utils.PackageUtils;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldSwitch;
import com.bald.uriah.baldphone.views.ModularRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.Collections;
import java.util.List;

/**
 * Activity for Sharing Photos, Videos and Contacts.
 * It has 2 Parts - sharing via Whatsapp, and sharing via other apps.
 * the whatsapp part the activity actually mostly defined in {@link BaseContactsActivity}
 */
public class ShareActivity extends BaseContactsActivity {
    private static final String TAG = ShareActivity.class.getSimpleName();
    public static final String EXTRA_SHARABLE_URI = "EXTRA_SHARABLE_URI";
    public static final String SORT_ORDER =
            "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC";
    private static final String STAR_SELECTION =
            "AND " + ContactsContract.Data.STARRED + " = 1";
    private static final String SELECTION_NAME =
            ContactsContract.Data.MIMETYPE + "='" + "vnd.android.cursor.item/vnd.com.whatsapp.profile" + "' AND " + ContactsContract.RawContacts.ACCOUNT_TYPE + "= ? AND " + ContactsContract.Data.DISPLAY_NAME + " LIKE ?";
    private Intent shareIntent;
    private BaldSwitch bald_switch;
    private ModularRecyclerView recyclerView;
    private View differently_container, whatsapp_container;
    private List<ResolveInfo> resolveInfoList = Collections.EMPTY_LIST;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        if (recyclerView != null && super.recyclerView.getAdapter().getItemCount() == 0) {
            differently_container.setVisibility(View.VISIBLE);
            whatsapp_container.setVisibility(View.GONE);
            bald_switch.setVisibility(View.GONE);
        }
    }

    @Override
    protected int layout() {
        return R.layout.activity_share;
    }

    @Override
    protected void viewsInit() {
        super.viewsInit();

        mode = ContactRecyclerViewAdapter.MODE_SHARE;
        final Intent callingIntent = getIntent();
        shareIntent = callingIntent.getParcelableExtra(EXTRA_SHARABLE_URI);
        resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);

        recyclerView.setAdapter(new IntentAdapter(this, resolveInfoList, (resolveInfo, context) -> context.startActivity(shareIntent.setPackage(resolveInfo.activityInfo.packageName))));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);//In Order to cover up the shitiness of loading resolve infos icons and texts
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .drawable(R.drawable.settings_divider)
                        .build()
        );
        recyclerView.getAdapter().notifyDataSetChanged();

        bald_switch.setOnChangeListener(isChecked -> {
            differently_container.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            whatsapp_container.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
            if (isChecked)
                S.hideKeyboard(this);
        });

    }

    @Override
    protected void attachXml() {
        super.attachXml();
        bald_switch = findViewById(R.id.bald_switch);
        recyclerView = findViewById(R.id.recycler_view);
        differently_container = findViewById(R.id.differently_container);
        whatsapp_container = findViewById(R.id.whatsapp_container);
    }

    @Override
    protected Cursor getCursorForFilter(String filter, boolean favorite) {
        return getContactsByNameFilter(filter, favorite);

    }

    private Cursor getContactsByNameFilter(String filter, boolean favorite) {
        final String[] args = {"com.whatsapp", "%" + filter + "%"};
        try {
            return contentResolver.query(ContactsContract.Data.CONTENT_URI,
                    ContactRecyclerViewAdapter.PROJECTION,
                    SELECTION_NAME + (favorite ? (STAR_SELECTION) : ("")),
                    args,
                    SORT_ORDER);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void whatsappShare(String lookupKey) {
        final Contact contact;
        try {
            contact = Contact.fromLookupKey(lookupKey, contentResolver);
        } catch (Contact.ContactNotFoundException e) {
            Log.e(TAG, S.str(e.getMessage()));
            e.printStackTrace();
            BaldToast.error(this);
            finish();
            return;
        }
        shareIntent.setPackage(PackageUtils.WHATSAPP_PACKAGE_NAME);
        String smsNumber = PhoneNumberUtils.stripSeparators(contact.getWhatsappNumbers().get(0)).replace("+", "").replace(" ", "");
        shareIntent.putExtra("jid", smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
        startActivity(shareIntent);
        finish();
    }
}
