package com.example.proyecto_iot.admin.model;

public class AdminRequestItem {
    private final String id;
    private final String name;
    private final String email;
    private final String subtitle;
    private final String description;
    private final String status;
    private final int avatarRes;
    private final String projectName;

    public AdminRequestItem(String id, String name, String email, String subtitle, String description, String status, int avatarRes, String projectName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.subtitle = subtitle;
        this.description = description;
        this.status = status;
        this.avatarRes = avatarRes;
        this.projectName = projectName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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

    public String getProjectName() {
        return projectName;
    }
}
