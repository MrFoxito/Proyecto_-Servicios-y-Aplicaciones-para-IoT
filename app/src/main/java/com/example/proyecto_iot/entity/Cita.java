package com.example.proyecto_iot.entity;

public class Cita {
    private String id;
    private String clientName;
    private String propertyName;
    private String time;
    private String date; // format YYYY-MM-DD
    private String status; // "Confirmada", "Pendiente", "Pasada", "En Camino", "Cerrada", "No Conectada", "Reprogramada"
    private String proyecto; // nombre del proyecto/inmobiliaria
    private boolean hasCierre; // si la cita derivó en una separación

    // Constructor original (retrocompatibilidad)
    public Cita(String id, String clientName, String propertyName, String time, String date, String status) {
        this(id, clientName, propertyName, time, date, status, "", false);
    }

    // Constructor completo
    public Cita(String id, String clientName, String propertyName, String time, String date, String status, String proyecto, boolean hasCierre) {
        this.id = id;
        this.clientName = clientName;
        this.propertyName = propertyName;
        this.time = time;
        this.date = date;
        this.status = status;
        this.proyecto = proyecto;
        this.hasCierre = hasCierre;
    }

    public String getId() { return id; }
    public String getClientName() { return clientName; }
    public String getPropertyName() { return propertyName; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getProyecto() { return proyecto; }
    public boolean hasCierre() { return hasCierre; }
}
