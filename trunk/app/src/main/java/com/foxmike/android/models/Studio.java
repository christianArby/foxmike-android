package com.foxmike.android.models;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by chris on 2018-07-05.
 */

public class Studio implements Serializable {
    private String hostId;
    private String sessionName;
    private String sessionType;
    private String maxParticipants;
    private double latitude;
    private double longitude;
    private HashMap<String, Long> sessions;
    private String imageUrl;
    private String what;
    private String who;
    private String where;
    private String duration;
    private String currency;
    private int price;

    public Studio(String hostId, String sessionName, String sessionType, String maxParticipants, double latitude, double longitude, HashMap<String, Long> sessions, String imageUrl, String what, String who, String where, String duration, String currency, int price) {
        this.hostId = hostId;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sessions = sessions;
        this.imageUrl = imageUrl;
        this.what = what;
        this.who = who;
        this.where = where;
        this.duration = duration;
        this.currency = currency;
        this.price = price;
    }

    public Studio() {
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(String maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public HashMap<String, Long> getSessions() {
        if (this.sessions==null) {
            sessions = new HashMap<String, Long>();
        }
        return sessions;
    }

    public void setSessions(HashMap<String, Long> sessions) {
        this.sessions = sessions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
