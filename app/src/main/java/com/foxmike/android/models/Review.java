package com.foxmike.android.models;

/**
 * Created by chris on 2019-03-18.
 */

public class Review {
    private String authorId;
    private String advertisementId;
    private String reviewText;
    private Long timestamp;

    public Review(String authorId, String advertisementId, String reviewText, Long timestamp) {
        this.authorId = authorId;
        this.advertisementId = advertisementId;
        this.reviewText = reviewText;
        this.timestamp = timestamp;
    }

    public Review() {
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
}
