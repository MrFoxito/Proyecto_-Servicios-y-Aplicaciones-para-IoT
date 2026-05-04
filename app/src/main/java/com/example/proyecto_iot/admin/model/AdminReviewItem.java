package com.example.proyecto_iot.admin.model;

public class AdminReviewItem {
    private final String reviewerName;
    private final String date;
    private final String projectName;
    private final String reviewText;
    private final String ratingLabel;
    private final int avatarRes;

    public AdminReviewItem(
            String reviewerName,
            String date,
            String projectName,
            String reviewText,
            String ratingLabel,
            int avatarRes
    ) {
        this.reviewerName = reviewerName;
        this.date = date;
        this.projectName = projectName;
        this.reviewText = reviewText;
        this.ratingLabel = ratingLabel;
        this.avatarRes = avatarRes;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getDate() {
        return date;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getRatingLabel() {
        return ratingLabel;
    }

    public int getAvatarRes() {
        return avatarRes;
    }
}
