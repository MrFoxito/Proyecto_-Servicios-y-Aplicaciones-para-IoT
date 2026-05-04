package com.example.proyecto_iot.usuario;

public class UsuarioPropertyListItem {

    private final String propertyId;
    private final String label;
    private final String title;
    private final String location;
    private final String price;
    private final int imageResId;

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price, int imageResId) {
        this.propertyId = propertyId;
        this.label = label;
        this.title = title;
        this.location = location;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getLabel() {
        return label;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}
