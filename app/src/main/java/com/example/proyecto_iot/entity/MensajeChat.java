package com.example.proyecto_iot.entity;

public class MensajeChat {
    private String id;
    private String conversacionId;
    private String remitenteId;   // asesor o cliente
    private String tipo;          // "texto", "imagen", "video"
    private String text;          // texto o URL de imagen/video
    private String time;
    private boolean sentByMe;
    private boolean isDateHeader;

    // Constructor para mensajes normales con ID
    public MensajeChat(String id, String text, String time, boolean sentByMe) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.sentByMe = sentByMe;
        this.isDateHeader = false;
    }

    // Constructor para mensajes rápidos (ficticios para pruebas)
    // Esto resuelve el error en AsesorChatIndividualActivity.java
    public MensajeChat(String text, String time, boolean sentByMe) {
        this.id = String.valueOf(System.currentTimeMillis());
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

    public void setId(String id) {
        this.id = id;
    }

    public String getConversacionId() {
        return conversacionId;
    }

    public void setConversacionId(String conversacionId) {
        this.conversacionId = conversacionId;
    }

    public String getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        this.sentByMe = sentByMe;
    }

    public boolean isDateHeader() {
        return isDateHeader;
    }

    public void setDateHeader(boolean dateHeader) {
        isDateHeader = dateHeader;
    }
}
