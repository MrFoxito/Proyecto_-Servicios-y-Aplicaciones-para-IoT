package com.example.proyecto_iot.admin.model;

public class AdminProjectVisualItem {
    private final String title;
    private String actionLabel;
    private int imageRes;
    private boolean hasImage;
    private String imageUri;

    public AdminProjectVisualItem(String title, String actionLabel, int imageRes, boolean hasImage) {
        this.title = title;
        this.actionLabel = actionLabel;
        this.imageRes = imageRes;
        this.hasImage = hasImage;
        this.imageUri = "";
    }

    public String getTitle() {
        return title;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public int getImageRes() {
        return imageRes;
    }

    public boolean hasImage() {
        return hasImage || (imageUri != null && !imageUri.isEmpty());
    }

    public String getImageUri() {
        return imageUri == null ? "" : imageUri;
    }

    public void setDeviceImageUri(String imageUri) {
        this.imageUri = imageUri == null ? "" : imageUri;
        this.hasImage = !this.imageUri.isEmpty() || imageRes != 0;
        this.actionLabel = hasImage() ? "Cambiar" : "Agregar foto";
    }

    public void clearDeviceImage() {
        this.imageUri = "";
        this.imageRes = 0;
        this.hasImage = false;
        this.actionLabel = "Agregar foto";
    }
}
