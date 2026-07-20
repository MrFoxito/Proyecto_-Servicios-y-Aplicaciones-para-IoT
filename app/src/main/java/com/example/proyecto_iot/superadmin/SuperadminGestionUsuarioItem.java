package com.example.proyecto_iot.superadmin;

public class SuperadminGestionUsuarioItem {
    private final String uid;
    private final String name;
    private final String email;
    private final String agency;
    private final String role;
    private final String avatarUrl;
    private boolean active;
    private final String dateIso;

    public SuperadminGestionUsuarioItem(String uid, String name, String email, String agency, String role,
                                       String avatarUrl, boolean active, String dateIso) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.agency = agency;
        this.role = role;
        this.avatarUrl = avatarUrl == null ? "" : avatarUrl.trim();
        this.active = active;
        this.dateIso = dateIso;
    }

    public String getUid() {
        return uid;
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

    public String getRole() {
        return role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDateIso() {
        return dateIso;
    }
}
