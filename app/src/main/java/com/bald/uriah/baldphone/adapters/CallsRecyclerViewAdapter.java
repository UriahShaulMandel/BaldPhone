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

package com.bald.uriah.baldphone.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.core.BaldActivity;
import com.bald.uriah.baldphone.apps.dialer.DialerActivity;
import com.bald.uriah.baldphone.apps.contacts.AddContactActivity;
import com.bald.uriah.baldphone.apps.contacts.SingleContactActivity;
import com.bald.uriah.baldphone.databases.calls.Call;
import com.bald.uriah.baldphone.databases.contacts.MiniContact;
import com.bald.uriah.baldphone.core.BDB;
import com.bald.uriah.baldphone.core.BDialog;
import com.bald.uriah.baldphone.utils.ColorUtils;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.ModularRecyclerView;
import com.bumptech.glide.Glide;

import org.joda.time.DateTime;

import java.util.List;

public class CallsRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<CallsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = CallsRecyclerViewAdapter.class.getSimpleName();

    public static final int INCOMING_TYPE = 1;
    public static final int OUTGOING_TYPE = 2;
    public static final int MISSED_TYPE = 3;
    public static final int VOICEMAIL_TYPE = 4;

    public static final int REJECTED_TYPE = 5;
    public static final int BLOCKED_TYPE = 6;
    public static final int ANSWERED_EXTERNALLY_TYPE = 7;

    @ColorInt
    private final int textColorOnRegular;
    private final List<Call> callList;
    private final BaldActivity activity;
    private final LayoutInflater inflater;
    private final Drawable letterContactBackground, privateFace, face;
    private ColorUtils.RandomColorMaker randomColorMaker;

    public CallsRecyclerViewAdapter(List<Call> callList, BaldActivity activity) {
        this.callList = callList;
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final TypedValue typedValue = new TypedValue();
        final Resources.Theme theme = activity.getTheme();
        theme.resolveAttribute(R.attr.bald_decoration_on_button, typedValue, true);
        textColorOnRegular = typedValue.data;
        theme.resolveAttribute(R.attr.bald_background, typedValue, true);
        this.letterContactBackground = new ColorDrawable(typedValue.data);
        this.randomColorMaker = new ColorUtils.RandomColorMaker(typedValue.data);
        this.privateFace = activity.getDrawable(R.drawable.private_face_in_recent_calls);
        this.face = activity.getDrawable(R.drawable.face_in_recent_calls);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.calls_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.update(position);
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final int expandedSize = 150;
        private static final int notExpandedSize = 100;

        final ImageView profile_pic, iv_type;
        final TextView tv_type, contact_name, tv_time, image_letter, day;
        final View line;
        final FrameLayout fl_contact_only;
        final LinearLayout container, ll_contact_only;
        private boolean expanded;

        public ViewHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView;
            ll_contact_only = container.findViewById(R.id.ll_contact_only);
            profile_pic = container.findViewById(R.id.profile_pic);
            tv_type = container.findViewById(R.id.tv_call_type);
            iv_type = container.findViewById(R.id.iv_call_type);
            contact_name = container.findViewById(R.id.contact_name);
            tv_time = container.findViewById(R.id.tv_time);
            image_letter = container.findViewById(R.id.image_letter);
            line = container.findViewById(R.id.line);
            day = container.findViewById(R.id.day);
            fl_contact_only = container.findViewById(R.id.fl_contact_only);
            ll_contact_only.setOnClickListener(this);
        }

        public void update(int index) {
            final Call call = callList.get(index);
            final MiniContact miniContact = call.getMiniContact(activity);
            if (miniContact != null) {
                if (miniContact.photo == null) {
                    image_letter.setText(miniContact.name != null ?
                            miniContact.name.length() >= 1 ?
                                    miniContact.name.substring(0, 1).toUpperCase() :
                                    null :
                            null);
                    image_letter.setVisibility(View.VISIBLE);
                    Glide.with(profile_pic).load(
                            activity.colorful ?
                                    new ColorDrawable(randomColorMaker.generateColor(miniContact.lookupKey.hashCode())) :
                                    letterContactBackground
                    ).into(profile_pic);
                } else {
                    if (S.isValidContextForGlide(profile_pic.getContext()))
                        Glide.with(profile_pic).load(miniContact.photo).into(profile_pic);
                    image_letter.setVisibility(View.INVISIBLE);
                }
                contact_name.setText(miniContact.name);
                @ColorInt final int textColor = textColorOnRegular;
                contact_name.setTextColor(textColor);
                tv_time.setTextColor(textColor);
                tv_type.setTextColor(textColor);
            } else {
                image_letter.setVisibility(View.INVISIBLE);
                if (call.isPrivate()) {
                    Glide.with(profile_pic).load(privateFace).into(profile_pic);
                    contact_name.setText(R.string.private_number);
                } else {
                    Glide.with(profile_pic).load(face).into(profile_pic);
                    contact_name.setText(call.phoneNumber);
                }
            }

            setType(call.callType);
            setDay(
                    (index == 0 ||
                            new DateTime(callList.get(index - 1).dateTime).getDayOfYear() !=
                                    new DateTime(call.dateTime).getDayOfYear()) ?
                            S.stringTimeFromLong(activity, call.dateTime, false) :
                            null
            );

            final DateTime dateTime = new DateTime(call.dateTime);
            tv_time.setText(S.numberToAlarmString(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        }

        public void setDay(final @Nullable String day) {
            if (day == null && expanded) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) this.container.getLayoutParams();
                layoutParams.height =
                        (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                notExpandedSize,
                                activity.getResources().getDisplayMetrics()
                        );
                this.container.setLayoutParams(layoutParams);
                this.line.setVisibility(View.GONE);
                this.day.setVisibility(View.GONE);
                expanded = false;
            } else if (day != null && expanded) {
                this.day.setText(day);
            } else if (day != null) {
                final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) this.container.getLayoutParams();
                layoutParams.height =
                        (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                expandedSize,
                                activity.getResources().getDisplayMetrics()
                        );
                this.container.setLayoutParams(layoutParams);
                this.line.setVisibility(View.VISIBLE);
                this.day.setVisibility(View.VISIBLE);
                this.day.setText(day);
                expanded = true;
            }
        }

        public void setType(int type) {
            @ColorRes final int colorRes;
            @DrawableRes final int drawableRes;
            @StringRes final int stringRes;
            switch (type) {
                case INCOMING_TYPE:
                case ANSWERED_EXTERNALLY_TYPE:
                    drawableRes = R.drawable.call_received_on_button;
                    stringRes = R.string.received;
                    colorRes = R.color.received;
                    break;
                case MISSED_TYPE:
                case REJECTED_TYPE:
                    drawableRes = R.drawable.call_missed_on_button;
                    stringRes = R.string.missed;
                    colorRes = R.color.missed;
                    break;
                case OUTGOING_TYPE:
                    drawableRes = R.drawable.call_made_on_button;
                    stringRes = R.string.outgoing;
                    colorRes = R.color.outgoing;
                    break;
                case VOICEMAIL_TYPE:
                    drawableRes = R.drawable.voicemail_on_button;
                    stringRes = R.string.voice_mail;
                    colorRes = R.color.other;
                    break;
                case BLOCKED_TYPE:
                    drawableRes = R.drawable.blocked_on_button;
                    stringRes = R.string.blocked;
                    colorRes = R.color.other;
                    break;
                default:
                    drawableRes = R.drawable.error_on_background;
                    stringRes = R.string.empty;
                    colorRes = R.color.other;
            }
            tv_type.setText(stringRes);
            iv_type.setImageResource(drawableRes);
            this.fl_contact_only.setBackgroundResource(colorRes);

        }

        @Override
        public void onClick(View v) {
            final Call call = callList.get(getAdapterPosition());
            final MiniContact miniContact = call.getMiniContact(activity);
            if (miniContact != null) {
                activity.startActivity(
                        new Intent(activity, SingleContactActivity.class)
                                .putExtra(SingleContactActivity.CONTACT_LOOKUP_KEY, miniContact.lookupKey)
                );
            } else {
                if (!call.isPrivate())
                    BDB.from(activity)
                            .setSubText(String.format(activity.getString(R.string.what_do_you_want_to_do_with___), call.phoneNumber))
                            .setOptions(R.string.call, R.string.add_contact, R.string.message)
                            .addFlag(BDialog.FLAG_OK)
                            .setPositiveButtonListener(params -> {
                                final int option = (int) params[0];
                                switch (option) {
                                    case 0:
                                        DialerActivity.call(call.phoneNumber, activity, false);
                                        return true;
                                    case 1:
                                        activity.startActivity(new Intent(activity, AddContactActivity.class).putExtra(AddContactActivity.CONTACT_NUMBER, call.phoneNumber));
                                        return true;
                                    case 2:
                                        S.sendMessage(call.phoneNumber, activity);
                                        return true;
                                }
                                throw new IllegalArgumentException("option must be 0 or 1");
                            })
                            .show();
            }
        }
    }
}