package com.example.proyecto_iot.admin.model;

public class AdminProjectVisualItem {
    private final String title;
    private final String actionLabel;
    private final int imageRes;
    private final boolean hasImage;

    public AdminProjectVisualItem(String title, String actionLabel, int imageRes, boolean hasImage) {
        this.title = title;
        this.actionLabel = actionLabel;
        this.imageRes = imageRes;
        this.hasImage = hasImage;
    }

    public String getTitle() {
        return title;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public int getImageRes() {
        return imageRes;
    }

    public boolean hasImage() {
        return hasImage;
    }
}
