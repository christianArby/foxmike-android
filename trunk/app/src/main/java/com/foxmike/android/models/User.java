package com.foxmike.android.models;

import android.support.annotation.NonNull;

/**
 * Created by chris on 2017-06-28.
 */

public class User implements Comparable<User>{

    private String userId;
    private String stripeCustomerId;
    private Long stripeLastChange;
    public String firstName;
    public String lastName;
    public String aboutMe;
    public String fullName;
    public String userName;
    public String image;
    public String thumb_image;
    public boolean trainerMode;
    private String stripeAccountId;
    private boolean dontShowBookingText;


    public User(String userId, String stripeCustomerId, Long stripeLastChange, String firstName, String lastName, String aboutMe, String fullName, String userName, String image, String thumb_image, boolean trainerMode, String stripeAccountId, boolean dontShowBookingText) {
        this.userId = userId;
        this.stripeCustomerId = stripeCustomerId;
        this.stripeLastChange = stripeLastChange;
        this.firstName = firstName;
        this.lastName = lastName;
        this.aboutMe = aboutMe;
        this.fullName = fullName;
        this.userName = userName;
        this.image = image;
        this.thumb_image = thumb_image;
        this.trainerMode = trainerMode;
        this.stripeAccountId = stripeAccountId;
        this.dontShowBookingText = dontShowBookingText;
    }

    public User() {

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

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
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

    public boolean isTrainerMode() {
        return trainerMode;
    }

    public void setTrainerMode(boolean trainerMode) {
        this.trainerMode = trainerMode;
    }

    public String getFullName() {
        return this.getFirstName() + " " + this.getLastName();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public int compareTo(@NonNull User user) {
        return this.getFirstName().compareToIgnoreCase(user.getFirstName());
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public Long getStripeLastChange() {
        return stripeLastChange;
    }

    public void setStripeLastChange(Long stripeLastChange) {
        this.stripeLastChange = stripeLastChange;
    }

    public boolean isDontShowBookingText() {
        return dontShowBookingText;
    }

    public void setDontShowBookingText(boolean dontShowBookingText) {
        this.dontShowBookingText = dontShowBookingText;
    }
}
