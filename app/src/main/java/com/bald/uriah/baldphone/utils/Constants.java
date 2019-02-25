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

import android.net.Uri;
import android.provider.MediaStore;

public class Constants {

    /**
     * contains the constants used in
     * {@link com.bald.uriah.baldphone.activities.media.SinglePhotoActivity}
     * and
     * {@link com.bald.uriah.baldphone.activities.media.PhotosActivity}
     */
    public interface PhotosConstants {
        String SORT_ORDER = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        String[] PROJECTION = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DATA, MediaStore.Images.Thumbnails.DATA,};
        Uri IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * contains the constants used in
     * {@link com.bald.uriah.baldphone.activities.media.SingleVideoActivity}
     * and
     * {@link com.bald.uriah.baldphone.activities.media.VideosActivity}
     */
    public interface VideosConstants {
        String SORT_ORDER = MediaStore.Video.Media.DATE_TAKEN + " DESC";
        String[] PROJECTION = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_TAKEN, MediaStore.Video.Media.DATA,};
        Uri VIDEOS_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }
}
