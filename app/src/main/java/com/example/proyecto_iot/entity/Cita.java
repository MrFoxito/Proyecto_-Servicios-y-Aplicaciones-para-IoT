package com.example.proyecto_iot.entity;

public class Cita {
    private String id;
    private String clientName;
    private String propertyName;
    private String time;
    private String date; // format YYYY-MM-DD
    private String status; // "Confirmada", "Pendiente", "Pasada", "En Camino"
    private int propertyImageRes;

    public Cita(String id, String clientName, String propertyName, String time, String date, String status, int propertyImageRes) {
        this.id = id;
        this.clientName = clientName;
        this.propertyName = propertyName;
        this.time = time;
        this.date = date;
        this.status = status;
        this.propertyImageRes = propertyImageRes;
    }

    public String getId() { return id; }
    public String getClientName() { return clientName; }
    public String getPropertyName() { return propertyName; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public int getPropertyImageRes() { return propertyImageRes; }
}
