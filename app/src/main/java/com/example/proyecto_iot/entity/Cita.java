package com.example.proyecto_iot.entity;

import java.util.List;

public class Cita {
    private String id;
    private String clientName;
    private String propertyName;
    private String time;
    private String date; // format YYYY-MM-DD
    private String status; // "Confirmada", "Pendiente", "Pasada", "En Camino", "Cerrada", "No Conectada", "Reprogramada"
    private String proyecto; // nombre del proyecto/inmobiliaria
    private boolean hasCierre; // si la cita derivó en una separación
    private List<EventoCita> historial;

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
        this.historial = new java.util.ArrayList<>();
    }

    public String getId() { return id; }
    public String getClientName() { return clientName; }
    public String getPropertyName() { return propertyName; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getProyecto() { return proyecto; }
    public boolean hasCierre() { return hasCierre; }
    public List<EventoCita> getHistorial() { return historial; }

    public void setTime(String time) { this.time = time; }
    public void setDate(String date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }
    public void addEvento(EventoCita evento) { this.historial.add(evento); }
}
