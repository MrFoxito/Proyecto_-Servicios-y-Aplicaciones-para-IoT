package com.example.proyecto_iot.usuario;

public class UsuarioTramiteItem {

    private final String title;
    private final String id;
    private final String status;
    private final String note;
    private final String due;
    private final boolean canPay;
    private final String projectId;

    public UsuarioTramiteItem(String title, String id, String status, String note, String due, boolean canPay, String projectId) {
        this.title = title;
        this.id = id;
        this.status = status;
        this.note = note;
        this.due = due;
        this.canPay = canPay;
        this.projectId = projectId;
    }

    public UsuarioTramiteItem(String title, String id, String status, String note, String due, boolean canPay) {
        this(title, id, status, note, due, canPay, "");
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getDue() {
        return due;
    }

    public boolean canPay() {
        return canPay;
    }

    public String getProjectId() {
        return projectId == null ? "" : projectId;
    }
}
