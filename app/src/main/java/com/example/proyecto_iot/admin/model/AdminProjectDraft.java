package com.example.proyecto_iot.admin.model;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectDraft {
    private final String projectName;
    private final String description;
    private final String address;
    private final String city;
    private final String mapLabel;
    private final String status;
    private final String deliveryDate;
    private final List<AdminProjectFormTypologyItem> typologies;
    private final List<AdminProjectFormAmenityItem> amenities;

    public AdminProjectDraft(
            String projectName,
            String description,
            String address,
            String city,
            String mapLabel,
            String status,
            String deliveryDate,
            List<AdminProjectFormTypologyItem> typologies,
            List<AdminProjectFormAmenityItem> amenities
    ) {
        this.projectName = projectName;
        this.description = description;
        this.address = address;
        this.city = city;
        this.mapLabel = mapLabel;
        this.status = status;
        this.deliveryDate = deliveryDate;
        this.typologies = new ArrayList<>(typologies);
        this.amenities = new ArrayList<>(amenities);
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getMapLabel() {
        return mapLabel;
    }

    public String getStatus() {
        return status;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public List<AdminProjectFormTypologyItem> getTypologies() {
        return new ArrayList<>(typologies);
    }

    public List<AdminProjectFormAmenityItem> getAmenities() {
        return new ArrayList<>(amenities);
    }
}
