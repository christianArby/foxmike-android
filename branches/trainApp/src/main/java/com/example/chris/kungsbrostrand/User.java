package com.example.chris.kungsbrostrand;

import java.util.ArrayList;

/**
 * Created by chris on 2017-06-28.
 */

public class User {
    public ArrayList<String> sessions;

    public User(ArrayList<String> sessions) {
        this.sessions = sessions;
    }

    public User() {
        this.sessions = new ArrayList<>();
    }
}
