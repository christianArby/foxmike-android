package com.foxmike.android.models;

import android.support.annotation.NonNull;

/**
 * Created by chris on 2018-11-29.
 */

public class UserPublic implements Comparable<UserPublic> {
    public String userId;
    public String firstName;
    public String lastName;
    public String fullName;
    public String aboutMe;
    public String image;
    public String thumb_image;
    public String userName;
    private float rating;
    private int nrOfRatings;

    public UserPublic(String userId, String firstName, String lastName, String fullName, String aboutMe, String image, String thumb_image, String userName, float rating, int nrOfRatings) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.aboutMe = aboutMe;
        this.image = image;
        this.thumb_image = thumb_image;
        this.userName = userName;
        this.rating = rating;
        this.nrOfRatings = nrOfRatings;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public UserPublic() {
    }

    public int getNrOfRatings() {
        return nrOfRatings;
    }

    public void setNrOfRatings(int nrOfRatings) {
        this.nrOfRatings = nrOfRatings;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public int compareTo(@NonNull UserPublic userPublic) {
        return this.getFirstName().compareToIgnoreCase(userPublic.getFirstName());
    }
}
