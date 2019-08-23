package com.foxmike.android.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;


public class Session implements Comparable<Session>, Serializable {
    private String sessionId;
    private String host;
    private String secondaryHostId;
    private String sessionName;
    private String sessionType;
    private String address;
    private int maxParticipants;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String imageUrlHiRes;
    private HashMap<String,String> images;
    private HashMap<String,String> imagesHQ;
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
    private boolean superHosted;
    private boolean plus;
    private String videoUrl;

    public Session(String sessionId, String host, String secondaryHostId, String sessionName, String sessionType, String address, int maxParticipants, double latitude, double longitude, String imageUrl, String imageUrlHiRes, HashMap<String, String> images, HashMap<String, String> imagesHQ, String what, String who, String whereAt, int durationInMin, String currency, long representingAdTimestamp, int price, float rating, int nrOfRatings, int nrOfReviews, boolean superHosted, boolean plus, String videoUrl) {
        this.sessionId = sessionId;
        this.host = host;
        this.secondaryHostId = secondaryHostId;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.address = address;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.imageUrlHiRes = imageUrlHiRes;
        this.images = images;
        this.imagesHQ = imagesHQ;
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
        this.superHosted = superHosted;
        this.plus = plus;
        this.videoUrl = videoUrl;
    }

    public Session() {
    }

    public Session(Session sessionToCopy) {
        this.sessionId = sessionToCopy.sessionId;
        this.host = sessionToCopy.host;
        this.secondaryHostId = sessionToCopy.secondaryHostId;
        this.sessionName = sessionToCopy.sessionName;
        this.sessionType = sessionToCopy.sessionType;
        this.address = sessionToCopy.address;
        this.maxParticipants = sessionToCopy.maxParticipants;
        this.latitude = sessionToCopy.latitude;
        this.longitude = sessionToCopy.longitude;
        this.imageUrl = sessionToCopy.imageUrl;
        this.imageUrl = sessionToCopy.imageUrlHiRes;
        this.images = sessionToCopy.images;
        this.imagesHQ = sessionToCopy.imagesHQ;
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
        this.plus = sessionToCopy.plus;
        this.videoUrl = sessionToCopy.videoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public HashMap<String, String> getImages() {
        if (this.images==null) {
            images = new HashMap<String, String>();
        }
        return images;
    }

    public void setImages(HashMap<String, String> images) {
        this.images = images;
    }



    public HashMap<String, String> getImagesHQ() {
        if (this.imagesHQ==null) {
            imagesHQ = new HashMap<String, String>();
        }
        return imagesHQ;
    }

    public void setImagesHQ(HashMap<String, String> imagesHQ) {
        this.imagesHQ = imagesHQ;
    }

    public boolean isSuperHosted() {
        return superHosted;
    }

    public void setSuperHosted(boolean superHosted) {
        this.superHosted = superHosted;
    }

    public long getRepresentingAdTimestamp() {
        return representingAdTimestamp;
    }

    public void setRepresentingAdTimestamp(long representingAdTimestamp) {
        this.representingAdTimestamp = representingAdTimestamp;
    }

    public String getSecondaryHostId() {
        return secondaryHostId;
    }

    public void setSecondaryHostId(String secondaryHostId) {
        this.secondaryHostId = secondaryHostId;
    }

    public String getImageUrlHiRes() {
        return imageUrlHiRes;
    }

    public void setImageUrlHiRes(String imageUrlHiRes) {
        this.imageUrlHiRes = imageUrlHiRes;
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

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
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

    public boolean isPlus() {
        return plus;
    }

    public void setPlus(boolean plus) {
        this.plus = plus;
    }
}
