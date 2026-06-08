package com.example.proyecto_iot.usuario;

public class UsuarioPropertyListItem {

    private final String propertyId;
    private final String label;
    private final String title;
    private final String location;
    private final String price;
    private final int imageResId;
    private final String imageUrl;
    private final String description;
    private final String district;
    private final String deliveryDate;
    private final String mapLabel;
    private final String bedrooms;
    private final String bathrooms;
    private final String area;
    private final String typologiesSummary;
    private final String amenitiesSummary;
    private final String status;

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price, int imageResId) {
        this(propertyId, label, title, location, price, imageResId, "");
    }

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price, int imageResId, String imageUrl) {
        this(
                propertyId,
                label,
                title,
                location,
                price,
                imageResId,
                imageUrl,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                label
        );
    }

    public UsuarioPropertyListItem(
            String propertyId,
            String label,
            String title,
            String location,
            String price,
            int imageResId,
            String imageUrl,
            String description,
            String district,
            String deliveryDate,
            String mapLabel,
            String bedrooms,
            String bathrooms,
            String area,
            String typologiesSummary,
            String amenitiesSummary,
            String status
    ) {
        this.propertyId = propertyId;
        this.label = label;
        this.title = title;
        this.location = location;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.description = description == null ? "" : description;
        this.district = district == null ? "" : district;
        this.deliveryDate = deliveryDate == null ? "" : deliveryDate;
        this.mapLabel = mapLabel == null ? "" : mapLabel;
        this.bedrooms = bedrooms == null ? "" : bedrooms;
        this.bathrooms = bathrooms == null ? "" : bathrooms;
        this.area = area == null ? "" : area;
        this.typologiesSummary = typologiesSummary == null ? "" : typologiesSummary;
        this.amenitiesSummary = amenitiesSummary == null ? "" : amenitiesSummary;
        this.status = status == null ? "" : status;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getDistrict() {
        return district;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public String getMapLabel() {
        return mapLabel;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public String getArea() {
        return area;
    }

    public String getTypologiesSummary() {
        return typologiesSummary;
    }

    public String getAmenitiesSummary() {
        return amenitiesSummary;
    }

    public String getStatus() {
        return status;
    }

    public String getListMeta() {
        if (!typologiesSummary.isEmpty()) {
            return location.isEmpty() ? typologiesSummary : location + " · " + typologiesSummary;
        }
        return location;
    }
}
