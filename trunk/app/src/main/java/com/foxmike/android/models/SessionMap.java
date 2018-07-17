package com.foxmike.android.models;

import android.support.annotation.NonNull;

/**
 * Created by chris on 2018-07-13.
 */

public class SessionMap implements Comparable<SessionMap>{
    private Session session;
    private int distance;

    public SessionMap() {
    }

    public SessionMap(Session session, int distance) {
        this.session = session;
        this.distance = distance;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(@NonNull SessionMap sessionMap) {
        int comp = this.getDistance()-sessionMap.getDistance();
        return (int) comp;
    }
}
