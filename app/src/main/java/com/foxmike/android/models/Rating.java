package com.foxmike.android.models;

/**
 * Created by chris on 2019-03-18.
 */

public class Rating {
    private String hostId;
    private String authorId;
    private String advertisementId;
    private String sessionId;
    private int rating;
    private Long timestamp;

    public Rating(String hostId, String authorId, String advertisementId, String sessionId, int rating, Long timestamp) {
        this.hostId = hostId;
        this.authorId = authorId;
        this.advertisementId = advertisementId;
        this.sessionId = sessionId;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public Rating() {
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(String advertisementId) {
        this.advertisementId = advertisementId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
