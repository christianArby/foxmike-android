package com.foxmike.android.models;

import android.support.annotation.NonNull;

/**
 * Created by chris on 2019-03-18.
 */

public class Review implements Comparable<Review>{
    private String authorId;
    private String advertisementId;
    private String sessionId;
    private String reviewText;
    private float rating;
    private Long timestamp;

    public Review(String authorId, String advertisementId, String sessionId, String reviewText, float rating, Long timestamp) {
        this.authorId = authorId;
        this.advertisementId = advertisementId;
        this.sessionId = sessionId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public Review() {
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
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

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
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
