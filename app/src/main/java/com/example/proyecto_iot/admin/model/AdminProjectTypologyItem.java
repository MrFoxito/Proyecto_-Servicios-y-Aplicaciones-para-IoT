package com.example.proyecto_iot.admin.model;

public class AdminProjectTypologyItem {
    private final String title;
    private final String status;
    private final boolean available;
    private final String area;
    private final String bedrooms;
    private final String totalAmount;
    private final String separationAmount;

    public AdminProjectTypologyItem(
            String title,
            String status,
            boolean available,
            String area,
            String bedrooms,
            String totalAmount,
            String separationAmount
    ) {
        this.title = title;
        this.status = status;
        this.available = available;
        this.area = area;
        this.bedrooms = bedrooms;
        this.totalAmount = totalAmount;
        this.separationAmount = separationAmount;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getArea() {
        return area;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getSeparationAmount() {
        return separationAmount;
    }
}
