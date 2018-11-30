package com.foxmike.android.models;

/**
 * Created by chris on 2018-11-29.
 */

public class UserImageUrlMap {
    String userImageUrl;
    String userThumbImageUrl;

    public UserImageUrlMap() {
    }

    public UserImageUrlMap(String userImageUrl, String userThumbImageUrl) {
        this.userImageUrl = userImageUrl;
        this.userThumbImageUrl = userThumbImageUrl;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserThumbImageUrl() {
        return userThumbImageUrl;
    }

    public void setUserThumbImageUrl(String userThumbImageUrl) {
        this.userThumbImageUrl = userThumbImageUrl;
    }
}
