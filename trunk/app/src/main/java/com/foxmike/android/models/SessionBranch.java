package com.foxmike.android.models;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by chris on 2018-03-19.
 */

public class SessionBranch implements Comparable<SessionBranch>{

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

        Calendar otherSessioncal = Calendar.getInstance();
        otherSessioncal.set(sessionBranch.getSession().getSessionDate().year, sessionBranch.getSession().getSessionDate().month, sessionBranch.getSession().getSessionDate().day, sessionBranch.getSession().getSessionDate().hour, sessionBranch.getSession().getSessionDate().minute);
        Date dateOfOtherSession = otherSessioncal.getTime();

        Calendar sessionCal = Calendar.getInstance();
        sessionCal.set(this.session.getSessionDate().getYear(), this.session.getSessionDate().month, this.session.getSessionDate().day, this.session.getSessionDate().hour, this.session.getSessionDate().minute);
        Date dateOfThisSession = sessionCal.getTime();


        return (dateOfThisSession.compareTo(dateOfOtherSession));
    }
}
