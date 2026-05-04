package com.example.proyecto_iot.superadmin;

public class SuperadminSolicitudAsesorItem {
    private final String name;
    private final String email;
    private final String agency;
    private final int avatarResId;
    private final String status;

    public SuperadminSolicitudAsesorItem(String name, String email, String agency, int avatarResId, String status) {
        this.name = name;
        this.email = email;
        this.agency = agency;
        this.avatarResId = avatarResId;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAgency() {
        return agency;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getStatus() {
        return status;
    }
}

