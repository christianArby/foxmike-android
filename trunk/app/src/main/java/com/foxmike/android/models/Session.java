package com.foxmike.android.models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;


public class Session implements Comparable<Session>, Serializable {
    private String sessionId;
    private String host;
    private String stripeAccountId;
    private String sessionName;
    private String sessionType;
    private String maxParticipants;
    private double latitude;
    private double longitude;
    private HashMap<String, Long> advertisements;
    private HashMap<String,String> participants  = new HashMap<String, String>();
    private HashMap<String,Boolean> posts;
    private String imageUrl;
    private String what;
    private String who;
    private String whereAt;
    private int durationInMin;
    private String currency;
    private long representingAdTimestamp;
    private int price;

    public Session(String sessionId, String host, String stripeAccountId, String sessionName, String sessionType, String maxParticipants, double latitude, double longitude, HashMap<String, Long> advertisements, HashMap<String, String> participants, HashMap<String, Boolean> posts, String imageUrl, String what, String who, String whereAt, int durationInMin, String currency, long representingAdTimestamp, int price) {
        this.sessionId = sessionId;
        this.host = host;
        this.stripeAccountId = stripeAccountId;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.advertisements = advertisements;
        this.participants = participants;
        this.posts = posts;
        this.imageUrl = imageUrl;
        this.what = what;
        this.who = who;
        this.whereAt = whereAt;
        this.durationInMin = durationInMin;
        this.currency = currency;
        this.representingAdTimestamp = representingAdTimestamp;
        this.price = price;
    }

    public Session() {
    }

    public Session(Session sessionToCopy) {
        this.sessionId = sessionToCopy.sessionId;
        this.host = sessionToCopy.host;
        this.stripeAccountId = sessionToCopy.stripeAccountId;
        this.sessionName = sessionToCopy.sessionName;
        this.sessionType = sessionToCopy.sessionType;
        this.maxParticipants = sessionToCopy.maxParticipants;
        this.latitude = sessionToCopy.latitude;
        this.longitude = sessionToCopy.longitude;
        this.advertisements = sessionToCopy.advertisements;
        this.participants = sessionToCopy.participants;
        this.posts = sessionToCopy.posts;
        this.imageUrl = sessionToCopy.imageUrl;
        this.what = sessionToCopy.what;
        this.who = sessionToCopy.who;
        this.whereAt = sessionToCopy.whereAt;
        this.durationInMin = sessionToCopy.durationInMin;
        this.currency = sessionToCopy.currency;
        this.representingAdTimestamp = sessionToCopy.representingAdTimestamp;
        this.price = sessionToCopy.price;
    }



    public long getRepresentingAdTimestamp() {
        return representingAdTimestamp;
    }

    public void setRepresentingAdTimestamp(long representingAdTimestamp) {
        this.representingAdTimestamp = representingAdTimestamp;
    }

    public HashMap<String, Long> getAdvertisements() {
        return advertisements;
    }

    public void setAdvertisements(HashMap<String, Long> advertisements) {
        this.advertisements = advertisements;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public int getDurationInMin() {
        return durationInMin;
    }

    public void setDurationInMin(int durationInMin) {
        this.durationInMin = durationInMin;
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

    @Override
    public int compareTo(@NonNull Session session) {

        /*Calendar otherSessioncal = Calendar.getInstance();
        otherSessioncal.setTimeInMillis(session.representingAdTimestamp);
        Date dateOfOtherSession = otherSessioncal.getTime();

        Calendar sessionCal = Calendar.getInstance();
        sessionCal.setTimeInMillis(this.representingAdTimestamp);
        Date dateOfThisSession = sessionCal.getTime();*/

        //return (dateOfThisSession.compareTo(dateOfOtherSession));

        long comp = this.getRepresentingAdTimestamp()-session.getRepresentingAdTimestamp();
        return (int) comp;

    }
}
