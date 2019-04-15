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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.Nullable;

/**
 * Most of this class is defined at {@link MediaScrollingActivity},
 * The Constants used are defined at {@link Constants.PhotosConstants}
 */
public class PhotosActivity extends MediaScrollingActivity implements Constants.PhotosConstants {
    private RequestOptions requestOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupYoutube(6);
    }

    @Override
    protected void setupBeforeAdapter() {
        requestOptions = new RequestOptions()
                .override(width)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(R.drawable.error_on_background)
                .lock();
    }

    @Override
    protected CharSequence title() {
        return getString(R.string.photos);
    }


    @Override
    protected Cursor cursor(ContentResolver contentResolver) {
        return contentResolver.query(IMAGES_URI, PROJECTION, null, null, SORT_ORDER);
    }

    @Override
    protected Class<? extends SingleMediaActivity> singleActivity() {
        return SinglePhotoActivity.class;
    }

    @Override
    protected void bindViewHolder(Cursor cursor, MediaRecyclerViewAdapter.ViewHolder holder) {
        final long imgId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        Cursor thumbnailCursor =
                MediaStore.Images.Thumbnails.queryMiniThumbnail(
                        getContentResolver(),
                        imgId,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null);
        if (thumbnailCursor != null && thumbnailCursor.getCount() > 0) {
            thumbnailCursor.moveToFirst();//**EDIT**
            Glide
                    .with(holder.itemView)
                    .load(thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)))
                    .apply(requestOptions)
                    .into(holder.pic);
        } else {
            thumbnailCursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(getContentResolver(), imgId, MediaStore.Images.Thumbnails.MICRO_KIND, null);
            if (thumbnailCursor != null && thumbnailCursor.getCount() > 0) {
                thumbnailCursor.moveToFirst();//**EDIT**
                Glide
                        .with(this)
                        .load(thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)))
                        .apply(requestOptions)
                        .into(holder.pic);
            } else {
                Glide
                        .with(this)
                        .load(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))
                        .apply(requestOptions)
                        .into(holder.pic);
            }
        }
        if (thumbnailCursor != null)
            thumbnailCursor.close();
    }

    @Override
    protected Uri getData(Cursor cursor) {
        return Uri.parse("file://" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
    }


}
