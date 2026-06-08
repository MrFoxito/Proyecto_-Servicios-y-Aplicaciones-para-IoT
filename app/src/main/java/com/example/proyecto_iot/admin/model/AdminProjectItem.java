package com.example.proyecto_iot.admin.model;

public class AdminProjectItem {
    private final String projectId;
    private final String title;
    private final String location;
    private final String priceFrom;
    private final String status;
    private final int imageRes;
    private final String imageUrl;

    public AdminProjectItem(String title, String location, String priceFrom, String status, int imageRes) {
        this(title, location, priceFrom, status, imageRes, "");
    }

    public AdminProjectItem(String title, String location, String priceFrom, String status, int imageRes, String imageUrl) {
        this("", title, location, priceFrom, status, imageRes, imageUrl);
    }

    public AdminProjectItem(String projectId, String title, String location, String priceFrom, String status, int imageRes, String imageUrl) {
        this.projectId = projectId == null ? "" : projectId;
        this.title = title;
        this.location = location;
        this.priceFrom = priceFrom;
        this.status = status;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getPriceFrom() {
        return priceFrom;
    }

    public String getStatus() {
        return status;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
