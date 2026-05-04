package com.example.proyecto_iot.admin.model;

import java.util.List;

public class AdminAdvisorItem {
    private final String name;
    private final String ratingText;
    private final String email;
    private final boolean active;
    private final int avatarRes;
    private final List<String> projects;

    public AdminAdvisorItem(String name, String ratingText, String email, boolean active, int avatarRes, List<String> projects) {
        this.name = name;
        this.ratingText = ratingText;
        this.email = email;
        this.active = active;
        this.avatarRes = avatarRes;
        this.projects = projects;
    }

    public String getName() {
        return name;
    }

    public String getRatingText() {
        return ratingText;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public int getAvatarRes() {
        return avatarRes;
    }

    public List<String> getProjects() {
        return projects;
    }
}
