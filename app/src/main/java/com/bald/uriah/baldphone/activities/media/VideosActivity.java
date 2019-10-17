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

package com.bald.uriah.baldphone.activities.media;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.utils.Constants;
import com.bald.uriah.baldphone.utils.S;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Most of this class is defined at {@link MediaScrollingActivity},
 * The Constants used are defined at {@link Constants.VideosConstants}
 */
public class VideosActivity extends MediaScrollingActivity implements Constants.VideosConstants {
    private RequestOptions requestOptions;
    private ContentResolver contentResolver;
    private BitmapFactory.Options options = new BitmapFactory.Options();

    @Override
    protected void setupBeforeAdapter() {
        options.inSampleSize = 1;

        requestOptions = new RequestOptions()
                .override(width)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(R.drawable.error_on_background)
                .lock();
        contentResolver = getContentResolver();
    }

    @Override
    protected CharSequence title() {
        return getString(R.string.videos);
    }

    @Override
    protected Cursor cursor(ContentResolver contentResolver) {
        return contentResolver.query(VIDEOS_URI, PROJECTION,
                null,
                null,
                SORT_ORDER
        );
    }

    @Override
    protected Class<? extends SingleMediaActivity> singleActivity() {
        return SingleVideoActivity.class;
    }

    @Override
    protected void bindViewHolder(Cursor cursor, MediaRecyclerViewAdapter.ViewHolder holder) {
        if (!S.isValidContextForGlide(holder.itemView.getContext()))
            return;

        final long vidId = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        Glide.with(holder.pic)
                .load(MediaStore.Video.Thumbnails.getThumbnail(contentResolver,
                        vidId,
                        MediaStore.Video.Thumbnails.MICRO_KIND,
                        options))
                .apply(requestOptions)
                .into(holder.pic);
    }

    @Override
    protected Uri getData(Cursor cursor) {
        return Uri.parse("file://" + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
    }
}
