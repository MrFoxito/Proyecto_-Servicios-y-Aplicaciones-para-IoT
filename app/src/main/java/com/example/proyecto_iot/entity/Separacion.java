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
    private double amount;
    private String currency;
    /** Snapshot of the configured property price at creation time. */
    private double precioTotal;
    private String precioTotalTexto;
    /** Snapshot of the configured separation amount at creation time. */
    private double montoSeparacion;
    private String montoSeparacionTexto;
    /** Campo de presentación resuelto desde proyectos; nunca se escribe en Firestore. */
    private String projectImageUrl;

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

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }

    public String getPrecioTotalTexto() { return precioTotalTexto; }
    public void setPrecioTotalTexto(String precioTotalTexto) { this.precioTotalTexto = precioTotalTexto; }

    public double getMontoSeparacion() { return montoSeparacion; }
    public void setMontoSeparacion(double montoSeparacion) { this.montoSeparacion = montoSeparacion; }

    public String getMontoSeparacionTexto() { return montoSeparacionTexto; }
    public void setMontoSeparacionTexto(String montoSeparacionTexto) { this.montoSeparacionTexto = montoSeparacionTexto; }

    public String getProjectImageUrl() { return projectImageUrl; }
    public void setProjectImageUrl(String projectImageUrl) { this.projectImageUrl = projectImageUrl; }
}
