package com.foxmike.android.utils;
// Checked

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.foxmike.android.models.UserImageUrlMap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

/**
 * Updates profile image and saves a thumbnail image
 */

public class SetOrUpdateUserImage {

    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageImage;
    private String currentUserID;
    private OnUserImageSetListener onUserImageSetListener;

    public SetOrUpdateUserImage() {
    }

    public void setOrUpdateUserImages(final Context context, Uri imageUri, String mCurrentUserID, OnUserImageSetListener onUserImageSetListener) {

        this.onUserImageSetListener = onUserImageSetListener;

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        this.currentUserID =mCurrentUserID;

        //Create bitmap thumb_image
        File thumb_filePath = new File(imageUri.getPath());
        Bitmap thumbBitmap;
        thumbBitmap = null;
        try {
            thumbBitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(75)
                    .compressToBitmap(thumb_filePath);
        } catch(IOException ie) {
            ie.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] thumb_byte = baos.toByteArray();

        final DatabaseReference current_user_db = mDatabaseUsers.child(currentUserID);
        final StorageReference thumb_filepath = mStorageImage.child("thumbs").child(currentUserID + ".jpg");
        StorageReference filepath = mStorageImage.child(currentUserID + ".jpg");

        filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        thumb_filepath.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        UserImageUrlMap userImageUrlMap = new UserImageUrlMap(downloadUrl, uri.toString());

                                        onUserImageSetListener.onUserImageSet(userImageUrlMap);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public interface OnUserImageSetListener {
        void onUserImageSet(UserImageUrlMap userImageUrlMap);
    }
}
