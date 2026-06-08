package com.example.proyecto_iot.superadmin;

public class SuperadminGestionUsuarioItem {
    private final String name;
    private final String email;
    private final String agency;
    private final int avatarResId;
    private final boolean active;
    private final String dateIso;

    public SuperadminGestionUsuarioItem(String name, String email, String agency, int avatarResId,
                                       boolean active, String dateIso) {
        this.name = name;
        this.email = email;
        this.agency = agency;
        this.avatarResId = avatarResId;
        this.active = active;
        this.dateIso = dateIso;
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

    public boolean isActive() {
        return active;
    }

    public String getDateIso() {
        return dateIso;
    }
}

