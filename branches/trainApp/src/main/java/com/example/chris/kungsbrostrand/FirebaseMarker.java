package com.example.chris.kungsbrostrand;

/**
 * Created by chris on 2017-05-14.
 */

public class FirebaseMarker {

    public String sessionType;
    public double latitude;
    public double longitude;


    //required empty constructor
    public FirebaseMarker() {
    }

    public FirebaseMarker(String sessionType, double latitude, double longitude) {
        this.sessionType = sessionType;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getsessionType() {
        return sessionType;
    }

    public void setsessionType(String sessionType) {
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
