package com.foxmike.android.models;

import android.net.Uri;

import com.foxmike.android.interfaces.OnUrlMapSetListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by chris on 2018-11-29.
 */

public class UserPublic {
    public String firstName;
    public String lastName;
    public String aboutMe;

    public UserPublic(String firstName, String lastName, String aboutMe) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.aboutMe = aboutMe;
    }

    public UserPublic() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public void getImagesDownloadUrls(String userId,OnUrlMapSetListener onUrlMapSetListener) {
        StorageReference imageFilepath = FirebaseStorage.getInstance().getReference().child("Profile_images").child(userId + ".jpg");
        StorageReference thumbImageFilepath = FirebaseStorage.getInstance().getReference().child("Profile_images").child("thumbs").child(userId + ".jpg");
        imageFilepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageDownloadUrl = uri.toString();
                thumbImageFilepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserImageUrlMap userImageUrlMap = new UserImageUrlMap();
                        userImageUrlMap.setUserImageUrl(imageDownloadUrl);
                        userImageUrlMap.setUserThumbImageUrl(uri.toString());
                        onUrlMapSetListener.OnUrlMapSet(userImageUrlMap);
                    }
                });
            }
        });
    }
}
