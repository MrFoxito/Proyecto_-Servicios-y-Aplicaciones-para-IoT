package com.example.proyecto_iot.admin.model;

public class AdminProjectGalleryItem {
    private final int imageRes;
    private final String imageUrl;

    public AdminProjectGalleryItem(int imageRes) {
        this(imageRes, "");
    }

    public AdminProjectGalleryItem(int imageRes, String imageUrl) {
        this.imageRes = imageRes;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
