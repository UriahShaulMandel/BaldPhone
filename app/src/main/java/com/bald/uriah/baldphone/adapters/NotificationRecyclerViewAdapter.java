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

package com.bald.uriah.baldphone.adapters;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.services.NotificationListenerService;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldPictureTextButton;
import com.bald.uriah.baldphone.views.ModularRecyclerView;

/**
 * using RecyclerView because of constant change of notification and NOT because of long scrolling list (most probably wont happen to elderly.)
 */
public class NotificationRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<NotificationRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = NotificationRecyclerViewAdapter.class.getSimpleName();
    private final static int MAX_LETTERS = 60;
    @ColorInt
    private final int decoration_on_button;
    private final Context context;
    private final DisplayMetrics displayMetrics;
    private final LayoutInflater layoutInflater;
    private final PackageManager packageManager;
    //        most probably reading will happen only once for each bundle.
    //          thus converting to old java objects wont really help..
    private Bundle[] bundles;

    public NotificationRecyclerViewAdapter(final Context context, final Bundle[] bundles) {
        this.context = context;
        this.bundles = bundles;
        layoutInflater = LayoutInflater.from(context);
        packageManager = context.getPackageManager();
        displayMetrics = context.getResources().getDisplayMetrics();

        final TypedValue typedValue = new TypedValue();
        final Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.bald_decoration_on_button, typedValue, true);
        decoration_on_button = typedValue.data;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.update(position);
    }

    @Override
    public int getItemCount() {
        return bundles.length;
    }

    public void clearAll() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        for (int i = 0; i < bundles.length; i++) {
            localBroadcastManager.sendBroadcast(
                    new Intent(NotificationListenerService.ACTION_CLEAR)
                            .putExtra(NotificationListenerService.KEY_EXTRA_KEY, bundles[i].getString(NotificationListenerService.KEY_EXTRA_KEY))

            );
        }

    }

    public void changeNotifications(Bundle[] bundles) {
        this.bundles = bundles;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView small_icon, large_icon;
        final TextView app_name, time_stamp, title, text;
        final BaldPictureTextButton clear;
        boolean first = true, summery = false;

        public ViewHolder(View itemView) {
            super(itemView);
            small_icon = itemView.findViewById(R.id.small_icon);
            large_icon = itemView.findViewById(R.id.large_icon);
            app_name = itemView.findViewById(R.id.app_name);
            time_stamp = itemView.findViewById(R.id.time_stamp);
            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
            clear = itemView.findViewById(R.id.clear);
        }

        public void update(final int position) {
            final Bundle item = bundles[position];
            final CharSequence app_name_string = item.getCharSequence("app_name");
            final boolean first = position == 0 || !bundles[position - 1].getCharSequence("app_name").equals(app_name_string);
            {
                final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        first ? 10 : 0,
                        displayMetrics);
                itemView.setLayoutParams(layoutParams);
            }

            final CharSequence packageName = item.getCharSequence("packageName");
            if (first) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final Icon icon = item.getParcelable("small_icon");
                    if (icon != null)
                        small_icon.setImageIcon(icon.setTint(decoration_on_button));
                } else {
                    try {
                        Resources resources = packageManager.getResourcesForApplication(String.valueOf(packageName));
                        final Drawable drawable = resources.getDrawable(item.getInt("small_icon"), null);
                        drawable.setTint(decoration_on_button);
                        small_icon.setImageDrawable(drawable);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                app_name.setText(app_name_string);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                large_icon.setImageIcon(item.getParcelable("large_icon"));
            } else {
                large_icon.setImageBitmap(item.getParcelable("large_item"));
            }

            large_icon.setVisibility(large_icon.getDrawable() == null ? View.GONE : View.VISIBLE);

            title.setText(item.getCharSequence("title"));
            CharSequence notification_text = item.getCharSequence("text");
            if (notification_text != null)
                if (notification_text.length() > MAX_LETTERS)
                    notification_text = String.valueOf(notification_text).substring(0, MAX_LETTERS).concat("...");
            this.text.setText(notification_text);
            final long timeStamp = item.getLong("time_stamp");
            if (timeStamp == 0L) {
                time_stamp.setText("");
            } else {
                time_stamp.setText(S.stringTimeFromLong(context, timeStamp, true));
            }
            final PendingIntent pendingIntent = item.getParcelable("content_intent");
            if (pendingIntent != null) {
                itemView.setOnClickListener((v) -> {
                    try {
                        pendingIntent.send();
                    } catch (Exception e) {
                        BaldToast.from(context).setText(R.string.an_error_has_occurred).setType(BaldToast.TYPE_ERROR).show();
                        Log.e(TAG, S.str(e.getMessage()));
                        e.printStackTrace();
                    }
                });
            } else
                itemView.setOnClickListener(null);
            final boolean clearable = item.getBoolean("clearable");
            boolean flag_summery = item.getBoolean("summery");
            if (flag_summery)
                if (isAlone(position, String.valueOf(app_name_string)))
                    flag_summery = false;
            if (this.first != first || this.summery != flag_summery) {
                if (this.summery != flag_summery) {
                    final int summeryVisibility = flag_summery ? View.GONE : View.VISIBLE;
                    this.text.setVisibility(summeryVisibility);
                    this.title.setVisibility(summeryVisibility);
                    this.large_icon.setVisibility(summeryVisibility);
                    this.clear.setVisibility(summeryVisibility);
                    this.time_stamp.setVisibility(summeryVisibility);
                }
                this.first = first;
                this.summery = flag_summery;
                final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        first ? flag_summery ? 60 : 160 : 130,
                        displayMetrics);
                itemView.setLayoutParams(layoutParams);
                final int firstVisibility = first ? View.VISIBLE : View.GONE;
                app_name.setVisibility(firstVisibility);
                small_icon.setVisibility(firstVisibility);
            }

            if (clearable) {
                clear.setVisibility(View.VISIBLE);
                clear.setOnClickListener((v) -> {
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(
                                    new Intent(NotificationListenerService.ACTION_CLEAR)
                                            .putExtra(NotificationListenerService.KEY_EXTRA_KEY, item.getString(NotificationListenerService.KEY_EXTRA_KEY))

                            );
                });
            } else {
                clear.setVisibility(View.GONE);
            }

        }

        private boolean isAlone(int position, final String app_name_string) {
            for (int i = 0; i < bundles.length; i++) {
                if (i == position)
                    continue;
                if (bundles[i].getString("app_name").equals(app_name_string))
                    return false;
            }
            return true;
        }

    }
}

