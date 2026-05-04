package com.example.proyecto_iot.entity;

public class Separacion {
    private String id;
    private String clientName;
    private String propertyName;
    private String price;
    private String date;
    private int imageRes;
    private String status; // e.g., "Pendiente", "Aprobado", "En Revisión"

    public Separacion(String id, String clientName, String propertyName, String price, String date, int imageRes, String status) {
        this.id = id;
        this.clientName = clientName;
        this.propertyName = propertyName;
        this.price = price;
        this.date = date;
        this.imageRes = imageRes;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
}
