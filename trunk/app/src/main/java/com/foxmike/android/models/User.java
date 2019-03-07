package com.foxmike.android.models;

import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * Created by chris on 2017-06-28.
 */

public class User implements Comparable<User>{

    private String userId;
    private HashMap<String,Object> stripeCustomer;
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


    public User(String userId, HashMap<String, Object> stripeCustomer, String firstName, String lastName, String aboutMe, String fullName, String userName, String image, String thumb_image, boolean trainerMode, String stripeAccountId, boolean dontShowBookingText) {
        this.userId = userId;
        this.stripeCustomer = stripeCustomer;
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
        this.stripeCustomer = new HashMap<String,Object>();
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

    public HashMap<String, Object> getStripeCustomer() {
        return stripeCustomer;
    }

    public void setStripeCustomer(HashMap<String, Object> stripeCustomer) {
        this.stripeCustomer = stripeCustomer;
    }

    public boolean isDontShowBookingText() {
        return dontShowBookingText;
    }

    public void setDontShowBookingText(boolean dontShowBookingText) {
        this.dontShowBookingText = dontShowBookingText;
    }
}
