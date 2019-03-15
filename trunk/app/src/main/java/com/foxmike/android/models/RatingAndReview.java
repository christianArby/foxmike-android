package com.foxmike.android.models;

/**
 * Created by chris on 2019-03-12.
 */

public class RatingAndReview {
    private float rating;
    private String review;
    private String authorId;
    private String subjectId;

    public RatingAndReview(float rating, String review, String authorId, String subjectId) {
        this.rating = rating;
        this.review = review;
        this.authorId = authorId;
        this.subjectId = subjectId;
    }

    public RatingAndReview() {
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
