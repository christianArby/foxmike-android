package com.foxmike.android.models;

import android.support.annotation.NonNull;

import com.foxmike.android.utils.TextTimestamp;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by chris on 2018-09-09.
 */

public class Advertisement implements Comparable<Advertisement> {
    private String sessionId;
    private String advertisementId;
    private String advertisementName;
    private String imageUrl;
    private String sessionType;
    private String host;
    private HashMap<String,String> participantsIds;
    private HashMap<String,Long> participantsTimestamps;
    private HashMap<String,Boolean> posts;
    private long advertisementTimestamp;
    private String currency;
    private int price;
    private String maxParticipants;

    public Advertisement() {
    }

    public Advertisement(String sessionId, String advertisementId, String advertisementName, String imageUrl, String sessionType, String host, HashMap<String, String> participantsIds, HashMap<String, Long> participantsTimestamps, HashMap<String, Boolean> posts, long advertisementTimestamp, String currency, int price, String maxParticipants) {
        this.sessionId = sessionId;
        this.advertisementId = advertisementId;
        this.advertisementName = advertisementName;
        this.imageUrl = imageUrl;
        this.sessionType = sessionType;
        this.host = host;
        this.participantsIds = participantsIds;
        this.participantsTimestamps = participantsTimestamps;
        this.posts = posts;
        this.advertisementTimestamp = advertisementTimestamp;
        this.currency = currency;
        this.price = price;
        this.maxParticipants = maxParticipants;
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
