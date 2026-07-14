package com.example.proyecto_iot.superadmin;

public class SuperadminGestionUsuarioItem {
    private final String uid;
    private final String name;
    private final String email;
    private final String agency;
    private final String role;
    private final int avatarResId;
    private boolean active;
    private final String dateIso;

    public SuperadminGestionUsuarioItem(String uid, String name, String email, String agency, String role,
                                       int avatarResId, boolean active, String dateIso) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.agency = agency;
        this.role = role;
        this.avatarResId = avatarResId;
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

    public int getAvatarResId() {
        return avatarResId;
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
