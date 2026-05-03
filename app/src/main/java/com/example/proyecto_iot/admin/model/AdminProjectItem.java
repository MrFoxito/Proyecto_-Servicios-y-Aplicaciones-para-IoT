package com.example.proyecto_iot.admin.model;

public class AdminProjectItem {
    private final String title;
    private final String location;
    private final String priceFrom;
    private final String status;
    private final int imageRes;

    public AdminProjectItem(String title, String location, String priceFrom, String status, int imageRes) {
        this.title = title;
        this.location = location;
        this.priceFrom = priceFrom;
        this.status = status;
        this.imageRes = imageRes;
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
}
