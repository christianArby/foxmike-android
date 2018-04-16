package com.foxmike.android.models;

import android.support.annotation.NonNull;

/**
 * Created by chris on 2018-03-31.
 */

public class UserBranch implements Comparable <UserBranch>{

    private String userID;
    private User user;

    public UserBranch(String userID, User user) {
        this.userID = userID;
        this.user = user;
    }

    public UserBranch() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int compareTo(@NonNull UserBranch userBranch) {
        return this.getUser().getFirstName().compareToIgnoreCase(userBranch.getUser().getFirstName());
    }
}
