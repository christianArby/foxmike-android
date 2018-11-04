package com.foxmike.android.models;

import android.support.annotation.NonNull;

import com.foxmike.android.utils.TextTimestamp;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by chris on 2018-09-09.
 */

public class Advertisement implements Comparable<Advertisement> {
    private String status;
    private String sessionId;
    private String advertisementId;
    private String host;
    private String advertisementName;
    private String sessionType;
    private String maxParticipants;
    private double latitude;
    private double longitude;
    private HashMap<String,String> participantsIds;
    private HashMap<String,Long> participantsTimestamps;
    private HashMap<String,Boolean> posts;
    private String imageUrl;
    private String what;
    private String who;
    private String whereAt;
    private int durationInMin;
    private String currency;
    private long advertisementTimestamp;
    private int price;


    public Advertisement() {
    }

    public Advertisement(String status, String sessionId, String advertisementId, String host, String advertisementName, String sessionType, String maxParticipants, double latitude, double longitude, HashMap<String, String> participantsIds, HashMap<String, Long> participantsTimestamps, HashMap<String, Boolean> posts, String imageUrl, String what, String who, String whereAt, int durationInMin, String currency, long advertisementTimestamp, int price) {
        this.status = status;
        this.sessionId = sessionId;
        this.advertisementId = advertisementId;
        this.host = host;
        this.advertisementName = advertisementName;
        this.sessionType = sessionType;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.participantsIds = participantsIds;
        this.participantsTimestamps = participantsTimestamps;
        this.posts = posts;
        this.imageUrl = imageUrl;
        this.what = what;
        this.who = who;
        this.whereAt = whereAt;
        this.durationInMin = durationInMin;
        this.currency = currency;
        this.advertisementTimestamp = advertisementTimestamp;
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public void setDuration(int durationInMin) {
        this.durationInMin = durationInMin;
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

    public String getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(String advertisementId) {
        this.advertisementId = advertisementId;
    }

    public String getAdvertisementName() {
        return advertisementName;
    }

    public void setAdvertisementName(String advertisementName) {
        this.advertisementName = advertisementName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public HashMap<String, String> getParticipantsIds() {
        if (this.participantsIds==null) {
            participantsIds = new HashMap<String, String>();
        }
        return participantsIds;
    }

    public void setParticipantsIds(HashMap<String, String> participantsIds) {
        this.participantsIds = participantsIds;
    }


    public HashMap<String, Long> getParticipantsTimestamps() {
        if (this.participantsTimestamps==null) {
            participantsTimestamps = new HashMap<String, Long>();
        }
        return participantsTimestamps;
    }

    public void setParticipantsTimestamps(HashMap<String, Long> participantsTimestamps) {
        this.participantsTimestamps = participantsTimestamps;
    }

    public HashMap<String, Boolean> getPosts() {
        return posts;
    }

    public void setPosts(HashMap<String, Boolean> posts) {
        this.posts = posts;
    }

    public long getAdvertisementTimestamp() {
        return advertisementTimestamp;
    }

    public void setAdvertisementTimestamp(long advertisementTimestamp) {
        this.advertisementTimestamp = advertisementTimestamp;
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

    public String getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(String maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Date supplyDate() {
        return new Date(this.getAdvertisementTimestamp());
    }

    public TextTimestamp supplyTextTimeStamp() {
        return new TextTimestamp(this.getAdvertisementTimestamp());
    }

    @Override
    public int compareTo(@NonNull Advertisement advertisement) {
        long comp = this.getAdvertisementTimestamp()-advertisement.getAdvertisementTimestamp();
        return (int) comp;
    }
}
