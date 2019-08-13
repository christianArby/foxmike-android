package com.foxmike.android.models;

import androidx.annotation.NonNull;

import com.foxmike.android.utils.TextTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by chris on 2018-09-09.
 */

public class Advertisement implements Comparable<Advertisement>, Serializable {
    private String status;
    private String sessionId;
    private String host;
    private String advertisementId;
    private int maxParticipants;
    private HashMap<String,Long> participantsTimestamps;
    private int durationInMin;
    private String currency;
    private long advertisementTimestamp;
    private int price;

    public Advertisement() {
    }

    public Advertisement(String status, String sessionId, String host, String advertisementId, int maxParticipants, HashMap<String, Long> participantsTimestamps, int durationInMin, String currency, long advertisementTimestamp, int price) {
        this.status = status;
        this.sessionId = sessionId;
        this.host = host;
        this.advertisementId = advertisementId;
        this.maxParticipants = maxParticipants;
        this.participantsTimestamps = participantsTimestamps;
        this.durationInMin = durationInMin;
        this.currency = currency;
        this.advertisementTimestamp = advertisementTimestamp;
        this.price = price;
    }

    public int getDurationInMin() {
        return durationInMin;
    }

    public void setDurationInMin(int durationInMin) {
        this.durationInMin = durationInMin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
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
