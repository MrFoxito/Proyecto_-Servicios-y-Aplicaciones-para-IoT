package com.example.proyecto_iot.entity;

public class Proyecto {
    private String id;                 // "casa_blanca"
    private String nombre;             // "casa blanca"
    private String direccion;          // "Av. Miguel Grau cdra. 13, Lima 15003, Perú"
    private String distrito;           // "" (puede estar vacío)
    private String estado;             // "En preventa" o "En venta", etc.
    private String imageUrl;           // base64 (recomiendo usar URL real)
    private String precioDesde;        // "350,000 USD"
    private String fechaEntrega;       // "30/06/2026"
    private String badge;              // "EN PREVENTA" (otro campo similar)
    private String descripcion;        // (opcional para detalle)
    private double lat;
    private double lng;

    // Constructor vacío necesario para Firestore
    public Proyecto() {}

    // Getters y Setters (todos los campos)


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrecioDesde() {
        return precioDesde;
    }

    public void setPrecioDesde(String precioDesde) {
        this.precioDesde = precioDesde;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}