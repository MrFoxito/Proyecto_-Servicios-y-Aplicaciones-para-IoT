package com.example.proyecto_iot.entity;

public class MensajeChat {
    private String id;

    private String conversacionId;
    private String remitenteId;   // asesor o cliente
    private String tipo;          // "texto", "imagen", "video"
    private String text;    // texto o URL de imagen/video
    private String time;
    private boolean sentByMe;
    private boolean isDateHeader;

    // Constructor para mensajes normales
    public MensajeChat(String id, String text, String time, boolean sentByMe) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.sentByMe = sentByMe;
        this.isDateHeader = false;
    }

    // Constructor para headers de fecha
    public MensajeChat(String text, boolean isDateHeader) {
        this.text = text;
        this.isDateHeader = isDateHeader;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }

    public boolean isDateHeader() {
        return isDateHeader;
    }
}
