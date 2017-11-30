package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

/**
 * Created by chris on 2017-11-30.
 */

public class SetOrUpdateUserImage {

    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageImage;
    private String currentUserID;
    private OnUserImageSetListener onUserImageSetListener;

    public SetOrUpdateUserImage() {
    }

    public void setOrUpdateUserImages(final Context context, Uri imageUri, String mCurrentUserID) {

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

        StorageReference filepath = mStorageImage.child(imageUri.getLastPathSegment());

        filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final String downloadUri = taskSnapshot.getDownloadUrl().toString();

                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                        if(thumb_task.isSuccessful()) {

                            Map update_hashMap = new HashMap();
                            update_hashMap.put("image", downloadUri);
                            update_hashMap.put("thumb_image", thumb_downloadUrl);

                            mDatabaseUsers.child(currentUserID).updateChildren(update_hashMap);

                            onUserImageSetListener.onUserImageSet();



                        } else {
                            //TODO Handle error
                        }
                    }
                });
            }
        });


    }

    public void setOnUserImageSetListener(OnUserImageSetListener onUserImageSetListener) {
        this.onUserImageSetListener = onUserImageSetListener;
    }

    public interface OnUserImageSetListener {
        void onUserImageSet();
    }
}
