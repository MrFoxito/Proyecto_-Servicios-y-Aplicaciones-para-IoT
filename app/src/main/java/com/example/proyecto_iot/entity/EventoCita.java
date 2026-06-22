package com.example.proyecto_iot.entity;

/**
 * Representa un evento en el historial de una cita.
 *
 * Diseñado para almacenamiento NoSQL (e.g. Firestore):
 *   - Se almacena en la colección independiente 'eventos_cita'.
 *   - El campo {@code citaId} permite reconstruir la relación en consultas planas.
 *
 * Tipos de evento ("tipo"):
 *   "AGENDADA"      → Cita creada
 *   "CONFIRMADA"    → Cliente confirmó asistencia
 *   "REPROGRAMADA"  → Fecha/hora modificada
 *   "CANCELADA"     → Cita cancelada
 *   "ASISTENCIA"    → Se registró asistencia o inasistencia
 *   "SEPARACION"    → Se registró una separación derivada
 *   "NOTA"          → Nota libre añadida por el asesor
 */
public class EventoCita {

    private String id;
    private String citaId;
    private String titulo;
    private String detalle;
    private String fechaHora;
    private String tipo;
    private long createdAt;

    public EventoCita() { /* requerido por deserializadores NoSQL */ }

    public EventoCita(String id, String citaId, String titulo,
                      String detalle, String fechaHora, String tipo, long createdAt) {
        this.id        = id;
        this.citaId    = citaId;
        this.titulo    = titulo;
        this.detalle   = detalle;
        this.fechaHora = fechaHora;
        this.tipo      = tipo;
        this.createdAt = createdAt;
    }

    public String getId()        { return id; }
    public String getCitaId()    { return citaId; }
    public String getTitulo()    { return titulo; }
    public String getDetalle()   { return detalle; }
    public String getFechaHora() { return fechaHora; }
    public String getTipo()      { return tipo; }
    public long getCreatedAt()   { return createdAt; }

    public void setId(String id)               { this.id = id; }
    public void setCitaId(String citaId)       { this.citaId = citaId; }
    public void setTitulo(String titulo)       { this.titulo = titulo; }
    public void setDetalle(String detalle)     { this.detalle = detalle; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
    public void setTipo(String tipo)           { this.tipo = tipo; }
    public void setCreatedAt(long createdAt)   { this.createdAt = createdAt; }
}
