package com.example.proyecto_iot.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Cita {
    private String id;
    private String asesorId;
    private String clienteId;
    private String proyectoId;
    private String proyectoNombre;
    private String tipologiaId;
    private String clienteNombre;
    private String asesorNombre;
    private String fechaISO;          // "2026-06-25"
    private String hora;              // "10:00" (formato 24h)
    private int duracionMinutos = 60;
    private String estado;            // Confirmada, Pendiente, Pasada, Cancelada
    private boolean hasCierre;
    private long createdAt;
    private String nota;
    private List<EventoCita> historial;
    private List<String> participantUids;
    private String meetingPoint;
    private String imageKey;
    private String slotId;
    private Long updatedAt;

    // Constructor vacío para Firestore
    public Cita() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAsesorId() { return asesorId; }
    public void setAsesorId(String asesorId) { this.asesorId = asesorId; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getProyectoId() { return proyectoId; }
    public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

    public String getProyectoNombre() { return proyectoNombre; }
    public void setProyectoNombre(String proyectoNombre) { this.proyectoNombre = proyectoNombre; }

    public String getTipologiaId() { return tipologiaId; }
    public void setTipologiaId(String tipologiaId) { this.tipologiaId = tipologiaId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getAsesorNombre() { return asesorNombre; }
    public void setAsesorNombre(String asesorNombre) { this.asesorNombre = asesorNombre; }

    public String getFechaISO() { return fechaISO; }
    public void setFechaISO(String fechaISO) { this.fechaISO = fechaISO; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isHasCierre() { return hasCierre; }
    public void setHasCierre(boolean hasCierre) { this.hasCierre = hasCierre; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setCreatedAt(Object createdAt) {
        if (createdAt instanceof com.google.firebase.Timestamp) {
            this.createdAt = ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
        } else if (createdAt instanceof Number) {
            this.createdAt = ((Number) createdAt).longValue();
        } else if (createdAt instanceof String) {
            try { this.createdAt = Long.parseLong((String) createdAt); } catch (Exception ignored) {}
        }
    }

    public List<String> getParticipantUids() { return participantUids; }
    public void setParticipantUids(List<String> participantUids) { this.participantUids = participantUids; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public List<EventoCita> getHistorial() { return historial; }
    public void setHistorial(List<EventoCita> historial) { this.historial = historial; }

    public String getMeetingPoint() { return meetingPoint; }
    public void setMeetingPoint(String meetingPoint) { this.meetingPoint = meetingPoint; }

    public String getImageKey() { return imageKey; }
    public void setImageKey(String imageKey) { this.imageKey = imageKey; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }


    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    // Métodos de utilidad para mostrar fechas y horas

    public String getFechaFormateada() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(fechaISO);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            return outputFormat.format(date);
        } catch (Exception e) {
            return fechaISO;
        }
    }

    public String getHoraFormateada() {
        // Convierte "10:00" a "10:00 AM"
        try {
            String[] parts = hora.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            String ampm = (h >= 12) ? "PM" : "AM";
            int h12 = (h == 0) ? 12 : (h > 12 ? h - 12 : h);
            return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, ampm);
        } catch (Exception e) {
            return hora;
        }
    }

    public String getDateTimeFormateada() {
        return getFechaFormateada() + ", " + getHoraFormateada();
    }

    // Para ordenar por fecha y hora
    public long getDateTimeMillis() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date d = sdf.parse(fechaISO + " " + hora);
            return d.getTime();
        } catch (Exception e) {
            return 0;
        }
    }
}