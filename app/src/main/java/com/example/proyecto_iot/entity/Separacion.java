package com.example.proyecto_iot.entity;

public class Separacion {
    private String id;
    private String clienteId;
    private String asesorId;
    private String projectId;
    private String tipologiaId;
    private String adminId;

    private String clienteNombre;
    private String asesorNombre;
    private String inmuebleNombre;
    private String montoTexto;
    private String fechaTexto;
    private String estado;          // "Pagada", "Pendiente", "Aprobada", "Rechazada"
    private String createdByRole;   // "cliente" o "asesor"
    private long createdAt;
    private String citaId;          // Opcional: referencia a la cita

    public Separacion() {} // Constructor vacío para Firestore

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getAsesorId() { return asesorId; }
    public void setAsesorId(String asesorId) { this.asesorId = asesorId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTipologiaId() { return tipologiaId; }
    public void setTipologiaId(String tipologiaId) { this.tipologiaId = tipologiaId; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getAsesorNombre() { return asesorNombre; }
    public void setAsesorNombre(String asesorNombre) { this.asesorNombre = asesorNombre; }

    public String getInmuebleNombre() { return inmuebleNombre; }
    public void setInmuebleNombre(String inmuebleNombre) { this.inmuebleNombre = inmuebleNombre; }

    public String getMontoTexto() { return montoTexto; }
    public void setMontoTexto(String montoTexto) { this.montoTexto = montoTexto; }

    public String getFechaTexto() { return fechaTexto; }
    public void setFechaTexto(String fechaTexto) { this.fechaTexto = fechaTexto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCreatedByRole() { return createdByRole; }
    public void setCreatedByRole(String createdByRole) { this.createdByRole = createdByRole; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }
}