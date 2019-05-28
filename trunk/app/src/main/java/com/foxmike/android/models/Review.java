package com.foxmike.android.models;

import androidx.annotation.NonNull;

/**
 * Created by chris on 2019-03-18.
 */

public class Review implements Comparable<Review>{
    private String hostId;
    private String authorId;
    private String advertisementId;
    private String sessionId;
    private String review;
    private int rating;
    private Long timestamp;

    public Review(String hostId, String authorId, String advertisementId, String sessionId, String review, int rating, Long timestamp) {
        this.hostId = hostId;
        this.authorId = authorId;
        this.advertisementId = advertisementId;
        this.sessionId = sessionId;
        this.review = review;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public Review() {
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
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

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(@NonNull Review review) {

        long comp = review.getTimestamp()-this.getTimestamp();
        return (int) comp;

    }
}
