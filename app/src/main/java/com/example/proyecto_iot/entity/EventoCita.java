package com.example.proyecto_iot.entity;

/**
 * Representa un evento en el historial de una cita.
 *
 * Diseñado para almacenamiento NoSQL (e.g. Firestore):
 *   - Se almacenará como sub-colección o array embebido dentro del documento Cita.
 *   - El campo {@code citaId} permite reconstruir la relación en consultas planas.
 *
 * Tipos de evento ("tipo"):
 *   "AGENDADA"      → Cita creada
 *   "CONFIRMADA"    → Cliente confirmó asistencia
 *   "REPROGRAMADA"  → Fecha/hora modificada
 *   "SEPARACION"    → Se registró una separación derivada
 *   "NOTA"          → Nota libre añadida por el asesor
 */
public class EventoCita {

    /** ID del documento en la sub-colección (UUID generado en cliente). */
    private String id;

    /** ID de la Cita padre — permite joins en consultas planas. */
    private String citaId;

    /** Título corto del evento (se muestra en bold en la UI). */
    private String titulo;

    /** Descripción adicional del evento. Puede ser vacía. */
    private String detalle;

    /**
     * Fecha y hora del evento en ISO-8601 (yyyy-MM-dd'T'HH:mm).
     * Se usa String para independencia del SDK y compatibilidad con Firestore Timestamp → String.
     */
    private String fechaHora;

    /**
     * Tipo de evento. Uno de:
     * AGENDADA | CONFIRMADA | REPROGRAMADA | SEPARACION | NOTA
     */
    private String tipo;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public EventoCita() { /* requerido por deserializadores NoSQL */ }

    public EventoCita(String id, String citaId, String titulo,
                      String detalle, String fechaHora, String tipo) {
        this.id        = id;
        this.citaId    = citaId;
        this.titulo    = titulo;
        this.detalle   = detalle;
        this.fechaHora = fechaHora;
        this.tipo      = tipo;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getId()        { return id; }
    public String getCitaId()    { return citaId; }
    public String getTitulo()    { return titulo; }
    public String getDetalle()   { return detalle; }
    public String getFechaHora() { return fechaHora; }
    public String getTipo()      { return tipo; }

    // ─── Setters (para actualizaciones en memoria antes de persistir) ─────────

    public void setId(String id)               { this.id = id; }
    public void setCitaId(String citaId)       { this.citaId = citaId; }
    public void setTitulo(String titulo)       { this.titulo = titulo; }
    public void setDetalle(String detalle)     { this.detalle = detalle; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
    public void setTipo(String tipo)           { this.tipo = tipo; }
}
