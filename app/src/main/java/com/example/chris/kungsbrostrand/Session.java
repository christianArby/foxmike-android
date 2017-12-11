package com.example.chris.kungsbrostrand;

import java.util.HashMap;


public class Session {
    private String host;
    private String sessionName;
    private String sessionType;
    private String mlevel;
    private String maxParticipants;
    private String mDescription;
    private double latitude;
    private double longitude;
    public String time;
    private SessionDate sessionDate;
    private int countParticipants;
    private boolean advertised;


    private HashMap<String,Boolean> participants;
    private String imageUri;

    public Session(String host, String sessionName, String sessionType, String mlevel, String maxParticipants, double latitude, double longitude, String time, SessionDate sessionDate, int countParticipants, HashMap<String, Boolean> participants, String imageUri, String mDescription, boolean advertised) {
        this.host = host;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.mlevel = mlevel;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.sessionDate = sessionDate;
        this.countParticipants = countParticipants;
        this.participants = participants;
        this.imageUri = imageUri;
        this.mDescription = mDescription;
        this.advertised = advertised;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public boolean isAdvertised() {
        return advertised;
    }

    public void setAdvertised(boolean advertised) {
        this.advertised = advertised;
    }

    //required empty constructor
    public Session() {
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

    public String getMlevel() {
        return mlevel;
    }

    public void setMlevel(String mlevel) {
        this.mlevel = mlevel;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public SessionDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(SessionDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public int getCountParticipants() {
        return countParticipants;
    }

    public void setCountParticipants(int countParticipants) {
        this.countParticipants = countParticipants;
    }

    public HashMap<String, Boolean> getParticipants() {
        if (participants==null) {
            participants = new HashMap<String, Boolean>();
        }
        return participants;
    }

    public void setParticipants(HashMap<String, Boolean> participants) {
        this.participants = participants;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
