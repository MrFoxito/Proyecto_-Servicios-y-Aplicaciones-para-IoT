package com.example.proyecto_iot.admin.model;

public class AdminProjectFormAmenityItem {
    private final String title;
    private final int iconRes;
    private boolean selected;

    public AdminProjectFormAmenityItem(String title, int iconRes, boolean selected) {
        this.title = title;
        this.iconRes = iconRes;
        this.selected = selected;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
