package com.example.proyecto_iot.usuario;

public class UsuarioAppointmentItem {

    private final String title;
    private final String status;
    private final String dateTime;
    private final String advisor;
    private final int imageResId;
    private final String imageUrl;
    private final String location;
    private final String note;
    private final boolean confirmed;

    public UsuarioAppointmentItem(
            String title,
            String status,
            String dateTime,
            String advisor,
            int imageResId,
            String imageUrl,
            String location,
            String note,
            boolean confirmed
    ) {
        this.title = title;
        this.status = status;
        this.dateTime = dateTime;
        this.advisor = advisor;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.location = location;
        this.note = note;
        this.confirmed = confirmed;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getAdvisor() {
        return advisor;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public String getNote() {
        return note;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
