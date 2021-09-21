package com.bald.uriah.baldphone.apps.media;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * contains the constants used in
 * {@link SinglePhotoActivity}
 * and
 * {@link PhotosActivity}
 */
public interface PhotosConstants {
    String SORT_ORDER = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
    String[] PROJECTION = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DATA, MediaStore.Images.Thumbnails.DATA,};
    Uri IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
}
