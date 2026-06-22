package com.example.proyecto_iot.entity;

public class Chat {
    private String id;                // "conv_001"
    private String asesorId;
    private String clienteId;
    private String projectId;
    private String ultimoMensaje;
    private String ultimoMensajeFecha; // opcional

    // Para mostrar en el adaptador (datos del cliente)
    private String clienteNombre;
    private String clienteAvatarUrl;
    private boolean unread;

    // Constructor vacío para Firestore
    public Chat() {}


    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAsesorId() { return asesorId; }
    public void setAsesorId(String asesorId) { this.asesorId = asesorId; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getUltimoMensaje() { return ultimoMensaje; }
    public void setUltimoMensaje(String ultimoMensaje) { this.ultimoMensaje = ultimoMensaje; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteAvatarUrl() { return clienteAvatarUrl; }
    public void setClienteAvatarUrl(String clienteAvatarUrl) { this.clienteAvatarUrl = clienteAvatarUrl; }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getUltimoMensajeFecha() {
        return ultimoMensajeFecha;
    }

    public void setUltimoMensajeFecha(String ultimoMensajeFecha) {
        this.ultimoMensajeFecha = ultimoMensajeFecha;
    }
}