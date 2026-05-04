package com.example.proyecto_iot.admin.model;

public class AdminRequestItem {
    private final String name;
    private final String subtitle;
    private final String description;
    private final String status;
    private final int avatarRes;

    public AdminRequestItem(String name, String subtitle, String description, String status, int avatarRes) {
        this.name = name;
        this.subtitle = subtitle;
        this.description = description;
        this.status = status;
        this.avatarRes = avatarRes;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public int getAvatarRes() {
        return avatarRes;
    }
}
