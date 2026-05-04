package com.example.proyecto_iot.admin.model;

public class AdminProjectAmenityItem {
    private final String title;
    private final int iconRes;

    public AdminProjectAmenityItem(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }
}
