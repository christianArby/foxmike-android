package com.foxmike.android.models;

import java.io.Serializable;

/**
 * Created by chris on 2018-03-19.
 */

public class SessionBranch implements Serializable{

    private String sessionID;
    private Session session;

    public SessionBranch(String sessionID, Session session) {
        this.sessionID = sessionID;
        this.session = session;
    }

    public SessionBranch() {
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

}
