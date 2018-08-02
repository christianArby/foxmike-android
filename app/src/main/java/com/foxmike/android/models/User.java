package com.foxmike.android.models;

import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by chris on 2017-06-28.
 */

public class User implements Comparable<User>{

    public HashMap<String,Long> sessionsAttending;
    public HashMap<String,Boolean> sessionsHosting;
    public HashMap<String,Boolean> studios;
    public HashMap<String,Boolean> chats;
    public String firstName;
    public String lastName;
    public String fullName;
    public String userName;
    public String image;
    public String thumb_image;
    public boolean trainerMode;
    public String aboutMe;
    private String stripeAccountId;
    private HashMap<String,Object> stripeCustomer;
    private boolean dontShowBookingText;

    public User(HashMap<String, Long> sessionsAttending, HashMap<String, Boolean> sessionsHosting, HashMap<String, Boolean> studios, HashMap<String, Boolean> chats, String firstName, String lastName, String fullName, String userName, String image, String thumb_image, boolean trainerMode, String aboutMe, String stripeAccountId, HashMap<String, Object> stripeCustomer, boolean dontShowBookingText) {
        this.sessionsAttending = sessionsAttending;
        this.sessionsHosting = sessionsHosting;
        this.studios = studios;
        this.chats = chats;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.userName = userName;
        this.image = image;
        this.thumb_image = thumb_image;
        this.trainerMode = trainerMode;
        this.aboutMe = aboutMe;
        this.stripeAccountId = stripeAccountId;
        this.stripeCustomer = stripeCustomer;
        this.dontShowBookingText = dontShowBookingText;
    }

    public User() {
        this.sessionsAttending = new HashMap<String,Long>();
        this.sessionsHosting = new HashMap<String,Boolean>();
        this.studios = new HashMap<String,Boolean>();
        this.stripeCustomer = new HashMap<String,Object>();
    }

    public HashMap<String, Long> getSessionsAttending() {
        return sessionsAttending;
    }

    public void setSessionsAttending(HashMap<String, Long> sessionsAttending) {
        this.sessionsAttending = sessionsAttending;
    }

    public HashMap<String, Boolean> getSessionsHosting() {
        return sessionsHosting;
    }

    public void setSessionsHosting(HashMap<String, Boolean> sessionsHosting) {
        this.sessionsHosting = sessionsHosting;
    }

    public HashMap<String, Boolean> getChats() {
        return chats;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public void setChats(HashMap<String, Boolean> chats) {
        this.chats = chats;
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


    public HashMap<String, Boolean> getStudios() {
        return studios;
    }

    public void setStudios(HashMap<String, Boolean> studios) {
        this.studios = studios;
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
