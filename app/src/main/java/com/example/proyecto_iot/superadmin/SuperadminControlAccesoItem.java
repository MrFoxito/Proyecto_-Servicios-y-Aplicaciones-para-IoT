package com.example.proyecto_iot.superadmin;

public class SuperadminControlAccesoItem {
    private final String name;
    private final String role;
    private final int avatarResId;

    public SuperadminControlAccesoItem(String name, String role, int avatarResId) {
        this.name = name;
        this.role = role;
        this.avatarResId = avatarResId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public int getAvatarResId() {
        return avatarResId;
    }
}

