package com.foxmike.android.models;

import androidx.annotation.NonNull;

/**
 * Created by chris on 2018-07-13.
 */

public class AdvertisementDistanceMap implements Comparable<AdvertisementDistanceMap>{
    private Advertisement advertisement;
    private int distance;

    public AdvertisementDistanceMap() {
    }

    public AdvertisementDistanceMap(Advertisement advertisement, int distance) {
        this.advertisement = advertisement;
        this.distance = distance;
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(@NonNull AdvertisementDistanceMap advertisementDistanceMap) {
        int comp = this.getDistance()- advertisementDistanceMap.getDistance();
        return (int) comp;
    }
}
