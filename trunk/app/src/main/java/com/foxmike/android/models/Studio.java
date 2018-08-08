package com.foxmike.android.models;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by chris on 2018-07-05.
 */

public class Studio implements Serializable {
    private String hostId;
    private String studioName;
    private String studioType;
    private String description;
    private double latitude;
    private double longitude;
    private HashMap<String, Long> sessions;
    private String imageUrl;
    private String location;

    public Studio(String hostId, String studioName, String studioType, String description, double latitude, double longitude, HashMap<String, Long> sessions, String imageUrl, String location) {
        this.hostId = hostId;
        this.studioName = studioName;
        this.studioType = studioType;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sessions = sessions;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    public Studio() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getStudioName() {
        return studioName;
    }

    public void setStudioName(String studioName) {
        this.studioName = studioName;
    }

    public String getStudioType() {
        return studioType;
    }

    public void setStudioType(String studioType) {
        this.studioType = studioType;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
