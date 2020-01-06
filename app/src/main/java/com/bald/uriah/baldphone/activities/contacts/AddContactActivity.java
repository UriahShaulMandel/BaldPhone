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

package com.bald.uriah.baldphone.activities.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.activities.HomeScreenActivity;
import com.bald.uriah.baldphone.activities.media.PhotosActivity;
import com.bald.uriah.baldphone.databases.contacts.Contact;
import com.bald.uriah.baldphone.utils.BDB;
import com.bald.uriah.baldphone.utils.BDialog;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;
import com.bald.uriah.baldphone.views.BaldImageButton;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

/**
 * simple activity which can Add\Edit a simple contact.
 * in order to Create\Edit an advanced contact the user must click on the selected button
 */
public class AddContactActivity extends BaldActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();
    public static final String CONTACT_NUMBER = "CONTACT_NUMBER";
    private static final int SELECT_IMAGE_REQUEST_CODE = 3;
    private static final String DEFAULT_WHERE = ContactsContract.Data.CONTACT_ID + "= ?";
    private Contact currentContact;
    private String newPhoto;
    private EditText et_name, et_mobile_number, et_home_number, et_address, et_mail;
    private BaldImageButton iv_image, iv_delete;
    private View save;

    private static void addFullSizePhoto(int rawContactId, byte[] fullSizedPhotoData, final ContentResolver cr) throws IOException {
        final Uri baseUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId);
        final Uri displayPhotoUri = Uri.withAppendedPath(baseUri, ContactsContract.RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
        final AssetFileDescriptor fileDescriptor = cr.openAssetFileDescriptor(displayPhotoUri, "rw");
        final FileOutputStream photoStream = fileDescriptor.createOutputStream();
        photoStream.write(fullSizedPhotoData);
        photoStream.close();
        fileDescriptor.close();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        setContentView(R.layout.add__edit_conatct_activity);
        attachXml();

        final Intent callingIntent = getIntent();
        if (callingIntent == null)
            throw new IllegalStateException(TAG + " calling intent cannot be null!");

        final String contactLookupKey = callingIntent.getStringExtra(SingleContactActivity.CONTACT_LOOKUP_KEY);
        if (contactLookupKey != null)
            try {
                fillWithContact(contactLookupKey);
            } catch (IllegalStateException | Contact.ContactNotFoundException e) {
                // sometimes it may happen, that the lookup key changes during the transition;
                //  while very unlikely (happened to me once in 200 tests), it should be checked
                startActivity(new Intent(this, ContactsActivity.class));
                finish();
                return;
            }
        else {
            final CharSequence contactNumber = callingIntent.getCharSequenceExtra(CONTACT_NUMBER);
            if (contactNumber != null) {
                et_mobile_number.setText(contactNumber);
            }
        }
        iv_image.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(this, PhotosActivity.class).setAction(Intent.ACTION_GET_CONTENT), SELECT_IMAGE_REQUEST_CODE)
        );
        iv_delete.setOnClickListener(v -> {
            iv_image.setImageResource(R.drawable.photo_on_button);
            newPhoto = null;
            v.setVisibility(View.INVISIBLE);
        });
        save.setOnClickListener(v -> save());
        ((BaldTitleBar) findViewById(R.id.bald_title_bar)).getBt_back().setOnClickListener(v -> {
            if (safeToExit())
                finish();
            else
                showExitMessage();
        });
    }

    private void save() {
        final String name = String.valueOf(et_name.getText());
        if (TextUtils.isEmpty(name.replace(" ", ""))) {
            BaldToast.from(this).setType(BaldToast.TYPE_ERROR).setText(R.string.contact_must_has_name).show();
        } else if (!(currentContact != null ? update() : insert())) {
            BaldToast.from(this).setType(BaldToast.TYPE_ERROR).setText(R.string.contact_not_created).show();
        } else {
            finishAffinity();
            SingleContactActivity.newPictureAdded = true;//static vars are simpler and more rational in this case

            startActivity(new Intent(this, HomeScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            startActivity(new Intent(this, ContactsActivity.class));
            startActivity(
                    new Intent(this, SingleContactActivity.class)
                            .putExtra(SingleContactActivity.CONTACT_ID, String.valueOf(currentContact.getId()))
                            .putExtra(SingleContactActivity.PIC_URI_EXTRA, newPhoto)
            );
        }
    }

    private void attachXml() {
        et_name = findViewById(R.id.et_name);
        et_mobile_number = findViewById(R.id.et_mobile_number);
        et_home_number = findViewById(R.id.et_home_number);
        et_address = findViewById(R.id.et_address);
        et_mail = findViewById(R.id.et_mail);
        iv_image = findViewById(R.id.iv_image);
        save = findViewById(R.id.save);
        iv_delete = findViewById(R.id.iv_delete);
    }

    private void fillWithContact(String contactLookupKey) throws Contact.ContactNotFoundException {
        currentContact = Contact.fromLookupKey(contactLookupKey, getContentResolver());
        newPhoto = currentContact.getPhoto();
        et_mobile_number.setText(currentContact.getMobilePhone());
        et_home_number.setText(currentContact.getHomePhone());
        et_address.setText(currentContact.getAddress());
        et_name.setText(currentContact.getName());
        et_mail.setText(currentContact.getMail());

        if (currentContact.getPhoto() != null) {
            if (S.isValidContextForGlide(iv_image.getContext()))
                Glide.with(iv_image).load(Uri.parse(currentContact.getPhoto())).into(iv_image);
            iv_delete.setVisibility(View.VISIBLE);
        }
    }

    public boolean insert() {
        final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        final String name = String.valueOf(et_name.getText());

        operations.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.DIRTY, false).build());

        operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, null)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, null)
                        .build()
        );

        try {
            ContentProviderResult[] results =
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);

            try (Cursor contactsCursor = getContentResolver().query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    null,
                    ContactsContract.RawContacts._ID + "=?",
                    new String[]{String.valueOf(ContentUris.parseId(results[0].uri))},
                    null)) {
                if (!contactsCursor.moveToFirst()) throw new AssertionError("cursor is empty");
                final String contactId = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)
                );
                currentContact = Contact.fromId(contactId, getContentResolver());
            }
            return update();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * newUpdate doesn't work! using Delete and Create instead
     * great example (in kotlin) here:https://github.com/SimpleMobileTools/Simple-Contacts/blob/master/app/src/main/kotlin/com/simplemobiletools/contacts/pro/helpers/ContactsHelper.kt)
     *
     * @return true if everything went without any problems
     */
    public boolean update() {
        final ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        final int rawId = currentContact.getRawContactId(getContentResolver());

        final String[] args = {String.valueOf(currentContact.getId()), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        final String name = String.valueOf(et_name.getText());
        if (!TextUtils.isEmpty(name))
            operations.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                            .withSelection(DEFAULT_WHERE + " AND " + ContactsContract.Data.MIMETYPE + " = ?", args)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, null)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, null)
                            .build()
            );
        else
            return false;

        //Mobile number adder + remover
        final String mobilePhoneNumber = currentContact.getMobilePhone();
        if (mobilePhoneNumber != null)
            operations.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection(
                                    DEFAULT_WHERE + " AND " + ContactsContract.Data.MIMETYPE + " = ? " +
                                            "AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + "= ?"
                                    , new String[]{String.valueOf(currentContact.getId()),
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                            mobilePhoneNumber})
                            .build()
            );

        final String mNumber = String.valueOf(et_mobile_number.getText());
        if (!TextUtils.isEmpty(mNumber))
            operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawId))
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mNumber)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build()
            );

        //Home number adder + remover
        final String homePhoneNumber = currentContact.getHomePhone();
        if (homePhoneNumber != null)
            operations.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection(
                                    DEFAULT_WHERE + " AND " + ContactsContract.Data.MIMETYPE + " = ? " +
                                            "AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + "= ?"
                                    , new String[]{String.valueOf(currentContact.getId()),
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                            homePhoneNumber})
                            .build());
        final String hNumber = String.valueOf(et_home_number.getText());
        if (!TextUtils.isEmpty(hNumber))
            operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawId))
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, hNumber)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                            .build()
            );

        //mail number adder + remover
        final String beforeMail = currentContact.getMail();
        if (beforeMail != null)
            operations.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection(
                                    DEFAULT_WHERE + " AND " + ContactsContract.Data.MIMETYPE + " = ? " +
                                            "AND " + ContactsContract.CommonDataKinds.Email.ADDRESS + "= ?"
                                    , new String[]{String.valueOf(currentContact.getId()),
                                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                                            beforeMail})
                            .build());

        final String mail = String.valueOf(et_mail.getText());
        if (!TextUtils.isEmpty(mail))
            operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawId))
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, mail)
                            .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                                    ContactsContract.CommonDataKinds.Email.TYPE_MOBILE)
                            .build());

        //Addresses:
        final CharSequence beforeAddress = currentContact.getAddress();
        if (beforeAddress != null)
            operations.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection(
                                    String.format("%s AND %s = ? AND %s= ?",
                                            DEFAULT_WHERE,
                                            ContactsContract.Data.MIMETYPE,
                                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
                                    )
                                    , new String[]{String.valueOf(currentContact.getId()),
                                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                                            beforeAddress.toString()})
                            .build());

        final String address = String.valueOf(et_address.getText());
        if (!TextUtils.isEmpty(address))
            operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawId))
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address)
                            .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
                            .build());

        //Photo
        final String beforeImage = currentContact.getPhoto();
        if (beforeImage != null && (!beforeImage.equals(newPhoto))) {
            operations.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection(
                                    String.format("%s AND %s = ? ",
                                            DEFAULT_WHERE,
                                            ContactsContract.Data.MIMETYPE)
                                    , new String[]{String.valueOf(currentContact.getId()),
                                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE,
                                    })
                            .build());
        }

        //apply operations
        try {
            ContentProviderResult[] results =
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
            for (ContentProviderResult result : results) {
                Log.e("Update Result", result.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (newPhoto != null && !newPhoto.equals(beforeImage)) {
            try {
                final Uri photoUri = Uri.parse(newPhoto);
                final ExifInterface exifInterface;
                try (InputStream inputStream = getContentResolver().openInputStream(photoUri)) {
                    exifInterface = new ExifInterface(inputStream);
                }
                final int width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
                final int height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
                if (width != -1 && height != -1) {
                    final int smaller = Math.min(width, height);
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .apply(new RequestOptions().centerCrop().override(smaller))
                            .load(photoUri)
                            .into(new PhotoAdder(rawId, this, true));

                } else
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .thumbnail(0.1f)
                            .load(Uri.parse(newPhoto))
                            .into(new PhotoAdder(rawId, this, false));

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                BaldToast.error(this);
            }
        }
        return true;
    }

    private void setImage(Uri uri) {
        if (S.isValidContextForGlide(iv_image.getContext()))
            Glide.with(iv_image).load(uri).into(iv_image);
        newPhoto = uri.toString();
        iv_delete.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK)
            setImage(data.getData());
    }

    @Override
    public void onBackPressed() {
        if (safeToExit())
            super.onBackPressed();
        else {
            if (vibrator != null)
                vibrator.vibrate(D.vibetime);
            showExitMessage();
        }
    }

    private boolean safeToExit() {
        final String mobile_number = S.str(et_mobile_number.getText().toString());
        final String home_number = S.str(et_home_number.getText().toString());
        final String address = S.str(et_address.getText().toString());
        final String name = S.str(et_name.getText().toString());
        final String mail = S.str(et_mail.getText().toString());

        if (currentContact == null)
            return mobile_number == home_number && address == name && mail == mobile_number; // check all of them are empty

        final String _mobile_number = S.str(currentContact.getMobilePhone());
        final String _home_number = S.str(currentContact.getHomePhone());
        final String _address = S.str(currentContact.getAddress());
        final String _name = S.str(currentContact.getName());
        final String _mail = S.str(currentContact.getMail());

        return (et_mobile_number.length() == 0 && et_home_number.length() == 0 && et_address.length() == 0 && et_name.length() == 0 && et_mail.length() == 0)
                || (Objects.equals(newPhoto, currentContact.getPhoto())
                && mobile_number.equals(_mobile_number)
                && home_number.equals(_home_number)
                && address.equals(_address)
                && name.equals(_name)
                && mail.equals(_mail));
    }

    private void showExitMessage() {
        BDB.from(this)
                .addFlag(BDialog.FLAG_OK | BDialog.FLAG_CANCEL)
                .setSubText(R.string.do_you_want_to_save_your_changes)
                .setOptions(R.string.save, R.string.discard)
                .setPositiveButtonListener(params -> {
                    if (params[0].equals(1))
                        finish();
                    else
                        save();
                    return true;
                }).show();
    }

    static class PhotoAdder extends SimpleTarget<Bitmap> {
        private final int rawId;
        private final ContentResolver contentResolver;
        private final boolean cropped;

        PhotoAdder(int rawId, Context context, boolean cropped) {
            this.cropped = cropped;
            this.rawId = rawId;
            this.contentResolver = context.getApplicationContext().getContentResolver();
        }

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            if (!cropped) {
                final int dimension = Math.min(resource.getWidth(), resource.getHeight());
                resource = ThumbnailUtils.extractThumbnail(resource, dimension, dimension);
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resource.compress(Bitmap.CompressFormat.JPEG, 30, stream);
            try {
                addFullSizePhoto(rawId, stream.toByteArray(), contentResolver);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_WRITE_CONTACTS | PERMISSION_READ_CONTACTS | PERMISSION_WRITE_EXTERNAL_STORAGE;
    }
}
