package com.example.proyecto_iot.admin.model;

public class AdminAssignedProjectItem {
    private final String title;
    private final String location;
    private final String status;
    private final int imageRes;

    public AdminAssignedProjectItem(String title, String location, String status, int imageRes) {
        this.title = title;
        this.location = location;
        this.status = status;
        this.imageRes = imageRes;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public int getImageRes() {
        return imageRes;
    }
}
