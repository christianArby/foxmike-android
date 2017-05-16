package com.example.chris.kungsbrostrand;

/**
 * Created by chris on 2017-05-14.
 */

public class FirebaseMarker {

    public String photoURL;
    public double latitude;
    public double longitude;


    //required empty constructor
    public FirebaseMarker() {
    }

    public FirebaseMarker(String photoURL, double latitude, double longitude) {
        this.photoURL = photoURL;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
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
