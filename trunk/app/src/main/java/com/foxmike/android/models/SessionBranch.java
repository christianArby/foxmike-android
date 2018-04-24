package com.foxmike.android.models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chris on 2018-03-19.
 */

public class SessionBranch implements Comparable<SessionBranch>, Serializable{

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

    @Override
    public int compareTo(@NonNull SessionBranch sessionBranch) {

        /*Calendar otherSessioncal = Calendar.getInstance();
        otherSessioncal.setTimeInMillis(sessionBranch.getSession().getSessionTimestamp());
        Date dateOfOtherSession = otherSessioncal.getTime();

        Calendar sessionCal = Calendar.getInstance();
        sessionCal.setTimeInMillis(this.session.getSessionTimestamp());

        return (dateOfThisSession.compareTo(dateOfOtherSession));*/

        long comp = this.getSession().getSessionTimestamp()-sessionBranch.getSession().getSessionTimestamp();
        return (int) comp;
    }
}
