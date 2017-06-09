package com.example.chris.kungsbrostrand;

/**
 * Created by chris on 2017-06-09.
 */

public class Session {

    public String sessionType;
    public String date;
    public String time;
    public String level;
    public String nrOfParticipants;
    public double latitude;
    public double longitude;




    public Session(String sessionType, String date, String time, String level, String nrOfParticipants, double latitude, double longitude) {
        this.sessionType = sessionType;
        this.date = date;
        this.time = time;
        this.level = level;
        this.nrOfParticipants = nrOfParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //required empty constructor
    public Session() {
    }


    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLevel() {
        return level;
    }

    public String getNrOfParticipants() {
        return nrOfParticipants;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setNrOfParticipants(String nrOfParticipants) {
        this.nrOfParticipants = nrOfParticipants;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


}
