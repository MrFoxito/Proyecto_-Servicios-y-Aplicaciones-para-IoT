package com.example.proyecto_iot.entity;

public class MensajeChat {
    private String id;
    private String conversationId;
    private String senderId;
    private String texto;
    private String fechaHora;
    private long timestamp;

    // Campos para UI (no se guardan en Firestore)
    private boolean sentByMe;
    private boolean isDateHeader;

    public MensajeChat() {}

    // Constructor para UI
    public MensajeChat(String texto, String fechaHora, boolean sentByMe) {
        this.texto = texto;
        this.fechaHora = fechaHora;
        this.sentByMe = sentByMe;
        this.isDateHeader = false;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSentByMe() { return sentByMe; }
    public void setSentByMe(boolean sentByMe) { this.sentByMe = sentByMe; }

    public boolean isDateHeader() { return isDateHeader; }
    public void setDateHeader(boolean dateHeader) { isDateHeader = dateHeader; }

    public String getTime() {
        // Formatear hora (ej. "10:05 AM")
        if (fechaHora != null && fechaHora.contains("T")) {
            String[] parts = fechaHora.split("T");
            if (parts.length > 1) {
                String timePart = parts[1];
                if (timePart.length() >= 5) {
                    return timePart.substring(0, 5);
                }
            }
        }
        return "";
    }

    public String getDate() {
        if (fechaHora != null && fechaHora.contains("T")) {
            return fechaHora.split("T")[0];
        }
        return "";
    }
}