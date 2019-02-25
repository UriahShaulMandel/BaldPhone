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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.adapters.ContactRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.contacts.Contact;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldSwitch;
import com.bald.uriah.baldphone.views.ModularRecyclerView;
import com.bumptech.glide.Glide;
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
    private Intent shareIntent;
    private BaldSwitch bald_switch;
    private ModularRecyclerView recyclerView;
    private View differently_container, whatsapp_container;
    private List<ResolveInfo> resolveInfoList = Collections.EMPTY_LIST;
    private LayoutInflater layoutInflater;
    private PackageManager packageManager;

    @Override
    protected void init() {
        layoutInflater = LayoutInflater.from(this);
        mode = ContactRecyclerViewAdapter.MODE_SHARE;
        final Intent callingIntent = getIntent();
        shareIntent = callingIntent.getParcelableExtra(EXTRA_SHARABLE_URI);
        resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);
        packageManager = getPackageManager();
        recyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (super.recyclerView.getAdapter().getItemCount() == 0) {
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

        recyclerView.setAdapter(new ShareAdapter());
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);//In Order to cover up the shitiness of loading resolve infos icons and texts
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .drawable(R.drawable.settings_divider)
                        .build()
        );
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


    private final static String[] PROJECTION = {
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.Data._ID,
            ContactsContract.Data.PHOTO_URI,
            ContactsContract.Data.LOOKUP_KEY,
            ContactsContract.Data.STARRED,
            ContactsContract.Data.MIMETYPE};

    private static final String STAR_SELECTION =
            "AND " + ContactsContract.Data.STARRED + " = 1";

    private static final String SELECTION_NAME =
            ContactsContract.Data.MIMETYPE + "='" + "vnd.android.cursor.item/vnd.com.whatsapp.profile" + "' AND " + ContactsContract.RawContacts.ACCOUNT_TYPE + "= ? AND " + ContactsContract.Data.DISPLAY_NAME + " LIKE ?";

    public static final String SORT_ORDER =
            "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC";


    private Cursor getContactsByNameFilter(String filter, boolean favorite) {
        final String[] args = {"com.whatsapp", "%" + filter + "%"};
        try {
            return contentResolver.query(ContactsContract.Data.CONTENT_URI,
                    PROJECTION,
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
        shareIntent.setPackage(D.WHATSAPP_PACKAGE_NAME);
        String smsNumber = PhoneNumberUtils.stripSeparators(contact.getWhatsappNumbers().get(0)).replace("+", "").replace(" ", "");
        shareIntent.putExtra("jid", smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
        startActivity(shareIntent);
        finish();
    }


    private class ShareAdapter extends ModularRecyclerView.ModularAdapter<ShareAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.settings_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.update(position);

        }


        @Override
        public int getItemCount() {
            return resolveInfoList.size();
        }

        class ViewHolder extends ModularRecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView settings_icon;
            final TextView tv_settings_name;

            public ViewHolder(View itemView) {
                super(itemView);
                settings_icon = itemView.findViewById(R.id.setting_icon);
                tv_settings_name = itemView.findViewById(R.id.tv_setting_name);
                itemView.setOnClickListener(this);
            }

            public void update(int position) {
                final ResolveInfo resolveInfo = resolveInfoList.get(position);
                Glide.with(settings_icon)
                        .load(resolveInfo.loadIcon(packageManager))
                        .into(settings_icon);
                tv_settings_name.setText(resolveInfo.loadLabel(packageManager));
            }

            @Override
            public void onClick(View v) {
                startActivity(shareIntent.setPackage(resolveInfoList.get(getAdapterPosition()).activityInfo.packageName));
            }
        }
    }
}
