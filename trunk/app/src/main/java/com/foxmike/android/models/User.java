package com.foxmike.android.models;

import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by chris on 2017-06-28.
 */

public class User implements Comparable<User>{

    public HashMap<String,Boolean> sessionsAttending;
    public HashMap<String,Boolean> sessionsHosting;
    public HashMap<String,Boolean> chats;
    public String name;
    public String image;
    public String thumb_image;
    public boolean trainerMode;
    public String aboutMe;

    public User(HashMap<String, Boolean> sessionsAttending, HashMap<String, Boolean> sessionsHosting, HashMap<String, Boolean> chats, String name, String image, String thumb_image, boolean trainerMode, Long lastSeen, String aboutMe) {
        this.sessionsAttending = sessionsAttending;
        this.sessionsHosting = sessionsHosting;
        this.chats = chats;
        this.name = name;
        this.image = image;
        this.thumb_image = thumb_image;
        this.trainerMode = trainerMode;
        this.aboutMe = aboutMe;
    }

    public User() {
        this.sessionsAttending = new HashMap<String,Boolean>();
        this.sessionsHosting = new HashMap<String,Boolean>();
    }

    public HashMap<String, Boolean> getSessionsAttending() {
        return sessionsAttending;
    }

    public void setSessionsAttending(HashMap<String, Boolean> sessionsAttending) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public int compareTo(@NonNull User user) {
        return this.getName().compareToIgnoreCase(user.getName());
    }
}
