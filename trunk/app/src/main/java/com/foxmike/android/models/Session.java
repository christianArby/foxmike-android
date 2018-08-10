package com.foxmike.android.models;

import android.support.annotation.NonNull;

import com.foxmike.android.utils.TextTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;


public class Session implements Comparable<Session>, Serializable {
    private String sessionId;
    private String studioId;
    private String host;
    private String stripeAccountId;
    private String sessionName;
    private String sessionType;
    private String maxParticipants;
    private double latitude;
    private double longitude;
    private long sessionTimestamp;
    private HashMap<String,String> participants  = new HashMap<String, String>();
    private HashMap<String,Boolean> posts;
    private String imageUrl;
    private String what;
    private String who;
    private String whereAt;
    private String duration;
    private String currency;
    private int price;

    public Session(String sessionId, String studioId, String host, String stripeAccountId, String sessionName, String sessionType, String maxParticipants, double latitude, double longitude, long sessionTimestamp, HashMap<String, String> participants, HashMap<String, Boolean> posts, String imageUrl, String what, String who, String whereAt, String duration, String currency, int price) {
        this.sessionId = sessionId;
        this.studioId = studioId;
        this.host = host;
        this.stripeAccountId = stripeAccountId;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sessionTimestamp = sessionTimestamp;
        this.participants = participants;
        this.posts = posts;
        this.imageUrl = imageUrl;
        this.what = what;
        this.who = who;
        this.whereAt = whereAt;
        this.duration = duration;
        this.currency = currency;
        this.price = price;
    }

    public Session() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStudioId() {
        return studioId;
    }

    public void setStudioId(String studioId) {
        this.studioId = studioId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
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

    public long getSessionTimestamp() {
        return sessionTimestamp;
    }

    public void setSessionTimestamp(long sessionTimestamp) {
        this.sessionTimestamp = sessionTimestamp;
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

    public String getWhereAt() {
        return whereAt;
    }

    public void setWhereAt(String whereAt) {
        this.whereAt = whereAt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public HashMap<String, String> getParticipants() {
        if (this.participants==null) {
            participants = new HashMap<String, String>();
        }
        return participants;
    }

    public void setParticipants(HashMap<String, String> participants) {
        this.participants = participants;
    }

    public HashMap<String, Boolean> getPosts() {
        if (this.posts==null) {
            posts = new HashMap<String, Boolean>();
        }
        return posts;
    }

    public void setPosts(HashMap<String, Boolean> posts) {
        this.posts = posts;
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

    public Date supplyDate() {
        return new Date(this.getSessionTimestamp());
    }

    public TextTimestamp supplyTextTimeStamp() {
        return new TextTimestamp(this.sessionTimestamp);
    }

    @Override
    public int compareTo(@NonNull Session session) {

        /*Calendar otherSessioncal = Calendar.getInstance();
        otherSessioncal.setTimeInMillis(session.sessionTimestamp);
        Date dateOfOtherSession = otherSessioncal.getTime();

        Calendar sessionCal = Calendar.getInstance();
        sessionCal.setTimeInMillis(this.sessionTimestamp);
        Date dateOfThisSession = sessionCal.getTime();*/

        //return (dateOfThisSession.compareTo(dateOfOtherSession));

        long comp = this.getSessionTimestamp()-session.getSessionTimestamp();
        return (int) comp;

    }
}
