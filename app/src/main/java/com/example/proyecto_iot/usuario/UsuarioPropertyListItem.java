package com.example.proyecto_iot.usuario;

public class UsuarioPropertyListItem {

    private final String propertyId;
    private final String label;
    private final String title;
    private final String location;
    private final String price;
    private final int imageResId;
    private final String imageUrl;
    private final String estadoProyecto;
    private final String fechaEntrega;
    private final String qrValue;
    private final String typologiesSummary;
    private final double latitude;
    private final double longitude;
    private final String district;
    private final String city;

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price, int imageResId) {
        this(propertyId, label, title, location, price, imageResId, "");
    }

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price, int imageResId, String imageUrl) {
        this(propertyId, label, title, location, price, imageResId, imageUrl, "", "", "");
    }

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price,
                                   int imageResId, String imageUrl, String estadoProyecto,
                                   String fechaEntrega, String qrValue) {
        this(propertyId, label, title, location, price, imageResId, imageUrl, estadoProyecto, fechaEntrega, qrValue, "");
    }

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price,
                                   int imageResId, String imageUrl, String estadoProyecto,
                                   String fechaEntrega, String qrValue, String typologiesSummary) {
        this(propertyId, label, title, location, price, imageResId, imageUrl, estadoProyecto,
                fechaEntrega, qrValue, typologiesSummary, Double.NaN, Double.NaN);
    }

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price,
                                   int imageResId, String imageUrl, String estadoProyecto,
                                   String fechaEntrega, String qrValue, String typologiesSummary,
                                   double latitude, double longitude) {
        this(propertyId, label, title, location, price, imageResId, imageUrl, estadoProyecto,
                fechaEntrega, qrValue, typologiesSummary, latitude, longitude, "", "");
    }

    public UsuarioPropertyListItem(String propertyId, String label, String title, String location, String price,
                                   int imageResId, String imageUrl, String estadoProyecto,
                                   String fechaEntrega, String qrValue, String typologiesSummary,
                                   double latitude, double longitude, String district, String city) {
        this.propertyId = propertyId;
        this.label = label;
        this.title = title;
        this.location = location;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.estadoProyecto = estadoProyecto == null ? "" : estadoProyecto;
        this.fechaEntrega = fechaEntrega == null ? "" : fechaEntrega;
        this.qrValue = qrValue == null ? "" : qrValue;
        this.typologiesSummary = typologiesSummary == null ? "" : typologiesSummary;
        this.latitude = latitude;
        this.longitude = longitude;
        this.district = district == null ? "" : district.trim();
        this.city = city == null ? "" : city.trim();
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

    public String getEstadoProyecto() {
        return estadoProyecto;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public String getQrValue() {
        return qrValue;
    }

    public String getTypologiesSummary() {
        return typologiesSummary;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean hasCoordinates() {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    /** Location for discovery cards: never exposes a full postal address. */
    public String getExploreLocation() {
        return ExploreProjectPresentationPolicy.location(district, city);
    }
}
