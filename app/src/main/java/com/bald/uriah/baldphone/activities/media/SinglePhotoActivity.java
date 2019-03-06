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

package com.bald.uriah.baldphone.activities.media;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.Constants;
import com.bumptech.glide.Glide;

/**
 * Most of this class is defined at {@link SingleMediaActivity}
 * The Constants used are defined at {@link Constants.PhotosConstants}
 */
public class SinglePhotoActivity extends SingleMediaActivity implements Constants.PhotosConstants {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupYoutube(6);
    }

    @Override
    protected MediaPagerAdapter mediaPagerAdapter() {
        return new PhotoPagerAdapter(this);
    }

    @Override
    protected CharSequence title() {
        return getText(R.string.photo);
    }

    private static class PhotoPagerAdapter extends MediaPagerAdapter {
        private static final Uri EXTERNAL = MediaStore.Files.getContentUri("external");

        public PhotoPagerAdapter(SingleMediaActivity activity) {
            super(activity);
        }

        @Override
        protected void delete(Activity activity, Cursor cursor) {
            final int id =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            final Uri deleteUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            activity.getContentResolver().delete(
                    EXTERNAL,
                    MediaStore.MediaColumns.DATA + "=?",
                    new String[]{getPath(deleteUri)});

        }

        private String getPath(Uri uri) {
            try (Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null, null)) {
                final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        }

        @Override
        protected Intent share(Activity activity, Cursor cursor) {
            final int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            final Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            return new Intent(Intent.ACTION_SEND)
                    .setTypeAndNormalize("image/*")
                    .putExtra(Intent.EXTRA_STREAM, uri);
        }


        @Override
        protected Cursor cursor(Context context) {
            return context.getContentResolver().query(IMAGES_URI,
                    PROJECTION,
                    null,
                    null,
                    MediaStore.Images.Media.DATE_MODIFIED + " DESC"        // Ordering
            );
        }

        @Override
        protected void bindView(View view, Cursor cursor, Context context) {
            final ImageView pic = view.findViewById(R.id.pic);
            Glide.with(pic).load(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))).into(pic);
        }

        @Override
        protected View getView(Context context) {
            return LayoutInflater.from(context).inflate(R.layout.fragment_image, null, false);
        }
    }
}
