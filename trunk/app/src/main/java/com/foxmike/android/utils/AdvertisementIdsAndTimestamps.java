package com.foxmike.android.utils;

import android.support.annotation.NonNull;

/**
 * Created by chris on 2019-02-08.
 */

public class AdvertisementIdsAndTimestamps implements Comparable<AdvertisementIdsAndTimestamps>{
    String advertisementId;
    Long adTimestamp;

    public AdvertisementIdsAndTimestamps(String advertisementId, Long adTimestamp) {
        this.advertisementId = advertisementId;
        this.adTimestamp = adTimestamp;
    }

    public String getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(String advertisementId) {
        this.advertisementId = advertisementId;
    }

    public Long getAdTimestamp() {
        return adTimestamp;
    }

    public void setAdTimestamp(Long adTimestamp) {
        this.adTimestamp = adTimestamp;
    }

    @Override
    public int compareTo(@NonNull AdvertisementIdsAndTimestamps advertisementIdsAndTimestamps) {
        long comp = this.getAdTimestamp()-advertisementIdsAndTimestamps.getAdTimestamp();
        return (int) comp;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AdvertisementIdsAndTimestamps)) return false;
        AdvertisementIdsAndTimestamps other = (AdvertisementIdsAndTimestamps) obj;
        return (this.advertisementId.equals(other.advertisementId) && this.adTimestamp.equals(other.adTimestamp));
    }
}
