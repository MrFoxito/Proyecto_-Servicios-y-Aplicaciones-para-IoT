package com.example.proyecto_iot.admin.model;

public class AdminAssignableProjectItem {
    private final String title;
    private final String location;
    private final String neighborhood;
    private final String status;
    private final int imageRes;

    public AdminAssignableProjectItem(
            String title,
            String location,
            String neighborhood,
            String status,
            int imageRes
    ) {
        this.title = title;
        this.location = location;
        this.neighborhood = neighborhood;
        this.status = status;
        this.imageRes = imageRes;
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
}
