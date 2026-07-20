package com.example.proyecto_iot.superadmin;

public class SuperadminControlAccesoItem {
    private final String uid;
    private final String name;
    private final String role;
    private final String avatarUrl;

    public SuperadminControlAccesoItem(String uid, String name, String role, String avatarUrl) {
        this.uid = uid == null ? "" : uid;
        this.name = name;
        this.role = role;
        this.avatarUrl = avatarUrl == null ? "" : avatarUrl.trim();
    }

    public String getUid() { return uid; }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}

