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

package com.bald.uriah.baldphone.apps.sos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.core.BaldActivity;
import com.bald.uriah.baldphone.apps.phone.dialer.DialerActivity;
import com.bald.uriah.baldphone.apps.phone.contacts.ContactsActivity;
import com.bald.uriah.baldphone.adapters.ContactRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.contacts.MiniContact;
import com.bald.uriah.baldphone.views.BaldLinearLayoutButton;

import java.util.List;

public class SOSActivity extends BaldActivity {
    public static final int MAX_PINNED_CONTACTS = 2;
    private BaldLinearLayoutButton[] ec;
    private BaldLinearLayoutButton ecReal;

    private static void setupEC(BaldLinearLayoutButton baldLinearLayoutButton, MiniContact miniContact) {
        if (miniContact.photo != null)
            ((ImageView) baldLinearLayoutButton.getChildAt(0)).setImageURI(Uri.parse(miniContact.photo));
        else
            ((ImageView) baldLinearLayoutButton.getChildAt(0)).setImageResource(R.drawable.face_on_button);

        ((TextView) baldLinearLayoutButton.getChildAt(1)).setText(miniContact.name);
        baldLinearLayoutButton.setOnClickListener(v -> DialerActivity.call(miniContact, v.getContext()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        setContentView(R.layout.activity_sos);
        ec = new BaldLinearLayoutButton[]{findViewById(R.id.ec1), findViewById(R.id.ec2)};
        ecReal = findViewById(R.id.ec_real);
        setupYoutube(4);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final View.OnClickListener addContactListener = v -> startActivity(new Intent(v.getContext().getApplicationContext(), ContactsActivity.class).putExtra(ContactsActivity.INTENT_EXTRA_CONTACT_ADAPTER_MODE, ContactRecyclerViewAdapter.MODE_SOS));
        final List<MiniContact> miniContacts = SOSPinningUtils.getAllPinnedContacts(this);
        final int size = miniContacts == null ? 0 : miniContacts.size();
        for (int i = 0; i < MAX_PINNED_CONTACTS; i++) {
            if (size > i)
                setupEC(ec[i], miniContacts.get(i));
            else
                ec[i].setOnClickListener(addContactListener);
        }

        ecReal.setOnClickListener((v) -> callEmergencyNumber());
    }

    private void callEmergencyNumber() {
        DialerActivity.call("112", this, true);//should work 99.99% of the times
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_up);
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_CALL_PHONE | PERMISSION_READ_CONTACTS;
    }
}