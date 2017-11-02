package com.example.chris.kungsbrostrand;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

/**
 * Created by chris on 2017-06-28.
 */

class User {

    public HashMap<String,Boolean> sessionsAttending;
    public HashMap<String,Boolean> sessionsHosting;
    public String name;
    public String image;
    public boolean trainerMode;

    public User(HashMap<String, Boolean> sessionsAttending, HashMap<String, Boolean> sessionsHosting, String name, String image, boolean trainerMode) {
        this.sessionsAttending = sessionsAttending;
        this.sessionsHosting = sessionsHosting;
        this.name = name;
        this.image = image;
        this.trainerMode = trainerMode;
    }



    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();



    public User() {
        this.sessionsAttending = new HashMap<String,Boolean>();
        this.sessionsHosting = new HashMap<String,Boolean>();
    }

    public HashMap<String, Boolean> getSessionsAttending() {
        return sessionsAttending;
    }

    public HashMap<String,Boolean> getSessionsHosting() {
        return sessionsHosting;
    }

    public void setUserName(String name) {
        this.name = name;
    }

    public void setUserImageURL(String image) {
        this.image = image;
    }

    public void setSessionsAttending(HashMap<String, Boolean> sessionsAttending) {
        this.sessionsAttending = sessionsAttending;
    }

    public void setSessionsHosting(HashMap<String, Boolean> sessionsHosting) {
        this.sessionsHosting = sessionsHosting;
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

    public boolean isTrainerMode() {
        return trainerMode;
    }

    public void setTrainerMode(boolean trainerMode) {
        this.trainerMode = trainerMode;
    }

}
