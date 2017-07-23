package com.example.chris.kungsbrostrand;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

/**
 * Created by chris on 2017-06-28.
 */

public class User {

    public HashMap<String,Boolean> sessionsAttending;
    public HashMap<String,Boolean> sessionsHosting;


    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    public User(HashMap<String,Boolean> sessionsAttending, HashMap<String,Boolean> sessionsHosting) {
        this.sessionsAttending = sessionsAttending;
        this.sessionsHosting = sessionsHosting;
    }

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

    public void setSessionsAttending(HashMap<String, Boolean> sessionsAttending) {
        this.sessionsAttending = sessionsAttending;
    }

    public void setSessionsHosting(HashMap<String, Boolean> sessionsHosting) {
        this.sessionsHosting = sessionsHosting;
    }

}
