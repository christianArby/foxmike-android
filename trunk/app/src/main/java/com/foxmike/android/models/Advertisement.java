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
    private String sessionName;
    private String imageUrl;
    private String advertisementId;
    private String maxParticipants;
    private HashMap<String,String> participantsIds;
    private HashMap<String,Long> participantsTimestamps;
    private int durationInMin;
    private String currency;
    private long advertisementTimestamp;
    private int price;

    public Advertisement() {
    }

    public Advertisement(String status, String sessionId, String sessionName, String imageUrl, String advertisementId, String maxParticipants, HashMap<String, String> participantsIds, HashMap<String, Long> participantsTimestamps, int durationInMin, String currency, long advertisementTimestamp, int price) {
        this.status = status;
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.imageUrl = imageUrl;
        this.advertisementId = advertisementId;
        this.maxParticipants = maxParticipants;
        this.participantsIds = participantsIds;
        this.participantsTimestamps = participantsTimestamps;
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


    public String getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(String advertisementId) {
        this.advertisementId = advertisementId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDurationInMin(int durationInMin) {
        this.durationInMin = durationInMin;
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
