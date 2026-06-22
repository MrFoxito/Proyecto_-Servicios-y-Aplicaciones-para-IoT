package com.example.proyecto_iot.entity;

import java.util.List;

public class Cita {
    private String id;
    private String clientName;
    private String propertyName;
    private String time;
    private String date; // format YYYY-MM-DD
    private String status;
    private String proyecto;
    private boolean hasCierre;
    private List<EventoCita> historial;
    private String clienteId = "";
    private String asesorId = "";
    private String propertyId = "";
    private String fechaISO = "";
    private String slotId = "";
    private int durationMinutos = 60;
    private int capacidadHorario = 1;

    public Cita(String id, String clientName, String propertyName, String time, String date, String status) {
        this(id, clientName, propertyName, time, date, status, "", false);
    }

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
    public String getClienteId() { return clienteId; }
    public String getAsesorId() { return asesorId; }
    public String getPropertyId() { return propertyId; }
    public String getFechaISO() { return fechaISO; }
    public String getSlotId() { return slotId; }
    public int getDurationMinutos() { return durationMinutos; }
    public int getCapacidadHorario() { return capacidadHorario; }

    public void setTime(String time) { this.time = time; }
    public void setDate(String date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }
    public void addEvento(EventoCita evento) { this.historial.add(evento); }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public void setAsesorId(String asesorId) { this.asesorId = asesorId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public void setFechaISO(String fechaISO) { this.fechaISO = fechaISO; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public void setDurationMinutos(int durationMinutos) { this.durationMinutos = durationMinutos; }
    public void setCapacidadHorario(int capacidadHorario) { this.capacidadHorario = capacidadHorario; }
}
