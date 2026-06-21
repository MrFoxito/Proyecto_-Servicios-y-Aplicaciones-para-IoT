package com.example.proyecto_iot.admin.model;

public class AdminAssignableProjectItem {
    private final String projectId;
    private final String title;
    private final String location;
    private final String neighborhood;
    private final String status;
    private final int imageRes;
    private final String imageUrl;

    public AdminAssignableProjectItem(
            String title,
            String location,
            String neighborhood,
            String status,
            int imageRes
    ) {
        this("", title, location, neighborhood, status, imageRes, "");
    }

    public AdminAssignableProjectItem(
            String projectId,
            String title,
            String location,
            String neighborhood,
            String status,
            int imageRes,
            String imageUrl
    ) {
        this.projectId = projectId == null ? "" : projectId;
        this.title = title;
        this.location = location;
        this.neighborhood = neighborhood;
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

    public String getNeighborhood() {
        return neighborhood;
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
