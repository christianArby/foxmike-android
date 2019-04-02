package com.foxmike.android.models;

import android.support.annotation.NonNull;

import java.io.Serializable;


public class Session implements Comparable<Session>, Serializable {
    private String sessionId;
    private String host;
    private String sessionName;
    private String sessionType;
    private String address;
    private String maxParticipants;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String what;
    private String who;
    private String whereAt;
    private int durationInMin;
    private String currency;
    private long representingAdTimestamp;
    private int price;
    private float rating;
    private int nrOfRatings;
    private int nrOfReviews;

    public Session(String sessionId, String host, String sessionName, String sessionType, String address, String maxParticipants, double latitude, double longitude, String imageUrl, String what, String who, String whereAt, int durationInMin, String currency, long representingAdTimestamp, int price, float rating, int nrOfRatings, int nrOfReviews) {
        this.sessionId = sessionId;
        this.host = host;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.address = address;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.what = what;
        this.who = who;
        this.whereAt = whereAt;
        this.durationInMin = durationInMin;
        this.currency = currency;
        this.representingAdTimestamp = representingAdTimestamp;
        this.price = price;
        this.rating = rating;
        this.nrOfRatings = nrOfRatings;
        this.nrOfReviews = nrOfReviews;
    }

    public Session() {
    }

    public Session(Session sessionToCopy) {
        this.sessionId = sessionToCopy.sessionId;
        this.host = sessionToCopy.host;
        this.sessionName = sessionToCopy.sessionName;
        this.sessionType = sessionToCopy.sessionType;
        this.address = sessionToCopy.address;
        this.maxParticipants = sessionToCopy.maxParticipants;
        this.latitude = sessionToCopy.latitude;
        this.longitude = sessionToCopy.longitude;
        this.imageUrl = sessionToCopy.imageUrl;
        this.what = sessionToCopy.what;
        this.who = sessionToCopy.who;
        this.whereAt = sessionToCopy.whereAt;
        this.durationInMin = sessionToCopy.durationInMin;
        this.currency = sessionToCopy.currency;
        this.representingAdTimestamp = sessionToCopy.representingAdTimestamp;
        this.price = sessionToCopy.price;
        this.rating = sessionToCopy.rating;
        this.nrOfRatings = sessionToCopy.nrOfRatings;
        this.nrOfReviews = sessionToCopy.nrOfReviews;
    }



    public long getRepresentingAdTimestamp() {
        return representingAdTimestamp;
    }

    public void setRepresentingAdTimestamp(long representingAdTimestamp) {
        this.representingAdTimestamp = representingAdTimestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getNrOfRatings() {
        return nrOfRatings;
    }

    public void setNrOfRatings(int nrOfRatings) {
        this.nrOfRatings = nrOfRatings;
    }

    public int getNrOfReviews() {
        return nrOfReviews;
    }

    public void setNrOfReviews(int nrOfReviews) {
        this.nrOfReviews = nrOfReviews;
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
