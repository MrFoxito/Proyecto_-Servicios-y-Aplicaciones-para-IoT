package com.example.proyecto_iot.entity;

public class Separacion {
    private String id;
    private String clientId;
    private String asesorId;
    private String projectId;
    private String tipologiaId;

    private String clientName;
    private String propertyName; // Nombre del proyecto o unidad
    private String price;
    private String date;
    private int imageRes;
    private String status; // "borrador", "pendiente", "aprobado", "rechazado"
    private String verificableUrl; // Simulación para Firestore

    public Separacion(String id, String clientId, String asesorId, String projectId, String tipologiaId,
                      String clientName, String propertyName, String price, String date, int imageRes, String status) {
        this.id = id;
        this.clientId = clientId;
        this.asesorId = asesorId;
        this.projectId = projectId;
        this.tipologiaId = tipologiaId;
        this.clientName = clientName;
        this.propertyName = propertyName;
        this.price = price;
        this.date = date;
        this.imageRes = imageRes;
        this.status = status;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getAsesorId() { return asesorId; }
    public void setAsesorId(String asesorId) { this.asesorId = asesorId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTipologiaId() { return tipologiaId; }
    public void setTipologiaId(String tipologiaId) { this.tipologiaId = tipologiaId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVerificableUrl() { return verificableUrl; }
    public void setVerificableUrl(String verificableUrl) { this.verificableUrl = verificableUrl; }
}
