package com.example.proyecto_iot.admin.model;

public class AdminProjectFormTypologyItem {
    private final String title;
    private final boolean available;
    private final String area;
    private final String bedrooms;
    private final String bathrooms;
    private final String totalAmount;
    private final String separationAmount;

    public AdminProjectFormTypologyItem(
            String title,
            boolean available,
            String area,
            String bedrooms,
            String bathrooms,
            String totalAmount,
            String separationAmount
    ) {
        this.title = title;
        this.available = available;
        this.area = area;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.totalAmount = totalAmount;
        this.separationAmount = separationAmount;
    }

    public AdminProjectFormTypologyItem(
            String title,
            boolean available,
            String area,
            String bedrooms,
            String totalAmount,
            String separationAmount
    ) {
        this(title, available, area, bedrooms, "2 banos", totalAmount, separationAmount);
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getStatusLabel() {
        return available ? "DISPONIBLE" : "NO DISPONIBLE";
    }

    public String getArea() {
        return area;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getSeparationAmount() {
        return separationAmount;
    }
}
