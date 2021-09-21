package com.bald.uriah.baldphone.apps.media;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * contains the constants used in
 * {@link SingleVideoActivity}
 * and
 * {@link VideosActivity}
 */
public interface VideosConstants {
    String SORT_ORDER = MediaStore.Video.Media.DATE_TAKEN + " DESC";
    String[] PROJECTION = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_TAKEN, MediaStore.Video.Media.DATA,};
    Uri VIDEOS_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
}
