package com.example.proyecto_iot.usuario;

import com.example.proyecto_iot.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UsuarioPropertyCatalog {

    public static final String ID_VILLA_LUMINARA = "villa_luminara";
    public static final String ID_IRON_WORKS = "iron_works";
    public static final String ID_REFUGIO_CELESTE = "refugio_celeste";
    public static final String ID_CASA_MERIDIAN = "casa_meridian";
    public static final String ID_ATICO_DEL_PARQUE = "atico_del_parque";

    private UsuarioPropertyCatalog() {
    }

    public static final class Amenity {
        private final int iconResId;
        private final String title;
        private final String description;

        public Amenity(int iconResId, String title, String description) {
            this.iconResId = iconResId;
            this.title = title;
            this.description = description;
        }

        public int getIconResId() {
            return iconResId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }

    public static final class PropertyDetail {
        private final String id;
        private final String previewLabel;
        private final String title;
        private final String price;
        private final String location;
        private final String district;
        private final String badge;
        private final String shortSubtitle;
        private final String eta;
        private final String aboutDescription;
        private final String highlightOne;
        private final String highlightTwo;
        private final String mapSummary;
        private final String mapCta;
        private final int heroImageResId;
        private final Amenity[] amenities;

        public PropertyDetail(
                String id,
                String previewLabel,
                String title,
                String price,
                String location,
                String district,
                String badge,
                String shortSubtitle,
                String eta,
                String aboutDescription,
                String highlightOne,
                String highlightTwo,
                String mapSummary,
                String mapCta,
                int heroImageResId,
                Amenity[] amenities
        ) {
            this.id = id;
            this.previewLabel = previewLabel;
            this.title = title;
            this.price = price;
            this.location = location;
            this.district = district;
            this.badge = badge;
            this.shortSubtitle = shortSubtitle;
            this.eta = eta;
            this.aboutDescription = aboutDescription;
            this.highlightOne = highlightOne;
            this.highlightTwo = highlightTwo;
            this.mapSummary = mapSummary;
            this.mapCta = mapCta;
            this.heroImageResId = heroImageResId;
            this.amenities = amenities;
        }

        public String getId() {
            return id;
        }

        public String getPreviewLabel() {
            return previewLabel;
        }

        public String getTitle() {
            return title;
        }

        public String getPrice() {
            return price;
        }

        public String getLocation() {
            return location;
        }

        public String getDistrict() {
            return district;
        }

        public String getBadge() {
            return badge;
        }

        public String getShortSubtitle() {
            return shortSubtitle;
        }

        public String getEta() {
            return eta;
        }

        public String getAboutDescription() {
            return aboutDescription;
        }

        public String getHighlightOne() {
            return highlightOne;
        }

        public String getHighlightTwo() {
            return highlightTwo;
        }

        public String getMapSummary() {
            return mapSummary;
        }

        public String getMapCta() {
            return mapCta;
        }

        public int getHeroImageResId() {
            return heroImageResId;
        }

        public Amenity[] getAmenities() {
            return amenities;
        }
    }

    public static List<PropertyDetail> getExploreProperties() {
        List<PropertyDetail> items = new ArrayList<>();
        items.add(getById(ID_VILLA_LUMINARA));
        items.add(getById(ID_IRON_WORKS));
        items.add(getById(ID_REFUGIO_CELESTE));
        items.add(getById(ID_CASA_MERIDIAN));
        return items;
    }

    public static PropertyDetail getById(String id) {
        if (ID_VILLA_LUMINARA.equals(id)) {
            return new PropertyDetail(
                    ID_VILLA_LUMINARA,
                    "CURADURIA DESTACADA",
                    "Villa Luminara",
                    "1.450.000 EUR",
                    "Valencia, Espana",
                    "La Marina Residencial",
                    "EDICION LIMITADA",
                    "Residencia costera con una lectura contemporanea del lujo mediterraneo.",
                    "Q2 2026",
                    "Villa Luminara conecta terrazas amplias, materiales minerales y aperturas generosas para una vida pausada frente al mar. La propuesta combina privacidad, artesania y tecnologia domestica discreta.",
                    "CERTIFICACION ENERGETICA A",
                    "SERVICIO DE CONCIERGE",
                    "A 8 minutos del puerto deportivo y rodeada de gastronomia, clubes nauticos y circuitos peatonales frente al mar.",
                    "VER ENTORNO COSTERO",
                    R.drawable.user_featured_house,
                    new Amenity[] {
                            new Amenity(R.drawable.ic_user_activity, "Wellness studio", "Entrenamiento, recovery lounge y cabinas privadas."),
                            new Amenity(R.drawable.ic_user_explore, "Piscina panoramica", "Deck soleado con vistas abiertas al Mediterraneo."),
                            new Amenity(R.drawable.ic_user_chat, "Club social", "Sala de encuentros, catas y agenda curada para residentes."),
                            new Amenity(R.drawable.ic_user_shield, "Seguridad integral", "Acceso controlado, lobby privado y monitoreo 24/7.")
                    }
            );
        }
        if (ID_IRON_WORKS.equals(id)) {
            return new PropertyDetail(
                    ID_IRON_WORKS,
                    "LOFT INDUSTRIAL",
                    "The Iron Works",
                    "2.100.000 EUR",
                    "Brooklyn, NY",
                    "DUMBO Historic District",
                    "LOFT CURADO",
                    "Un loft editorial que mezcla estructura industrial y acabados hechos a medida.",
                    "Q1 2026",
                    "The Iron Works conserva la potencia del ladrillo, las vigas expuestas y la doble altura, pero los equilibra con carpinteria sobria, iluminacion escultural y una organizacion precisa de los ambientes.",
                    "DOBLE ALTURA +",
                    "ESTUDIO CREATIVO",
                    "A pasos del waterfront de DUMBO, galerias independientes, restaurantes de autor y conexiones rapidas hacia Manhattan.",
                    "VER ENTORNO URBANO",
                    R.drawable.user_popular_1,
                    new Amenity[] {
                            new Amenity(R.drawable.ic_user_activity, "Fitness room", "Espacio privado para entrenamiento funcional diario."),
                            new Amenity(R.drawable.ic_user_explore, "Roof terrace", "Mirador con skyline, comedor exterior y lounge."),
                            new Amenity(R.drawable.ic_user_chat, "Cowork atelier", "Mesas largas, booths privados y sala de presentaciones."),
                            new Amenity(R.drawable.ic_user_shield, "Lobby 24 horas", "Recepcion asistida y paqueteria premium.")
                    }
            );
        }
        if (ID_REFUGIO_CELESTE.equals(id)) {
            return new PropertyDetail(
                    ID_REFUGIO_CELESTE,
                    "COSTA AZUL",
                    "Refugio Celeste",
                    "4.890.000 EUR",
                    "Malibu, CA",
                    "Pacific Bluffs",
                    "PREVENTA",
                    "Arquitectura serena sobre la costa con lenguaje brutalista y calidez material.",
                    "Q4 2025",
                    "Refugio Celeste propone una residencia abierta al paisaje, con planos limpios, piedra calida y una circulacion pensada para contemplar el horizonte. Todo el proyecto privilegia silencio, luz y privacidad.",
                    "CERTIFICADO A+",
                    "DOMOTICA INTEGRADA",
                    "En un tramo alto de Pacific Coast Highway, cerca de clubes de playa, rutas escenicas y reservas naturales con acceso controlado.",
                    "VER EN MAPA LOCAL",
                    R.drawable.user_popular_2,
                    new Amenity[] {
                            new Amenity(R.drawable.ic_user_activity, "Gimnasio privado", "Equipamiento premium de ultima generacion y recovery zone."),
                            new Amenity(R.drawable.ic_user_explore, "Piscina infinity", "Vistas panoramicas sobre el skyline costero y terraza lounge."),
                            new Amenity(R.drawable.ic_user_chat, "Coworking lounge", "Espacios privados y areas de networking silencioso."),
                            new Amenity(R.drawable.ic_user_shield, "Seguridad 24/7", "Control de acceso biometrico y vigilancia perimetral.")
                    }
            );
        }
        if (ID_CASA_MERIDIAN.equals(id)) {
            return new PropertyDetail(
                    ID_CASA_MERIDIAN,
                    "NUEVA COLECCION",
                    "Casa Meridian",
                    "3.250.000 EUR",
                    "Lisbon, PT",
                    "Lapa Collection",
                    "RESERVA ABIERTA",
                    "Residencia urbana con interiores luminosos y una lectura sofisticada del patrimonio lisboeta.",
                    "Q3 2026",
                    "Casa Meridian ordena circulaciones tranquilas, patios de luz y materiales sobrios para una vida urbana sin ruido visual. El proyecto mira a la ciudad desde una escala domestica y muy cuidada.",
                    "INTERIORES A MEDIDA",
                    "BODEGA PRIVADA",
                    "En el corazon de Lapa, entre embajadas, plazas residenciales y acceso rapido al casco cultural de Lisboa.",
                    "VER ENTORNO HISTORICO",
                    R.drawable.user_featured_house,
                    new Amenity[] {
                            new Amenity(R.drawable.ic_user_activity, "Pilates room", "Sala privada con equipamiento ligero y vistas al patio."),
                            new Amenity(R.drawable.ic_user_explore, "Rooftop garden", "Jardin alto con comedor exterior y sunset deck."),
                            new Amenity(R.drawable.ic_user_chat, "Library lounge", "Lectura silenciosa, reuniones discretas y cava social."),
                            new Amenity(R.drawable.ic_user_shield, "Acceso privado", "Ingreso discreto con atencion remota y control inteligente.")
                    }
            );
        }
        if (ID_ATICO_DEL_PARQUE.equals(id)) {
            return new PropertyDetail(
                    ID_ATICO_DEL_PARQUE,
                    "EN VENTA",
                    "Atico del Parque",
                    "1.250.000 EUR",
                    "Madrid, Espana",
                    "Paseo del Olivo",
                    "VISITA PRIVADA",
                    "Un atico sereno con vistas abiertas, interiores sobrios y espacios para recibir con calma.",
                    "Entrega inmediata",
                    "Atico del Parque trabaja una escala domestica elegante: carpinteria en madera oscura, salas abiertas al exterior y una terraza principal que conecta la vivienda con el verde cercano.",
                    "TERRAZA PANORAMICA",
                    "SUITE PRINCIPAL XL",
                    "A minutos de Paseo del Olivo, con acceso a parques, oferta gastronomica y conexiones rapidas hacia el centro residencial.",
                    "VER ENTORNO RESIDENCIAL",
                    R.drawable.user_property_hero_real,
                    new Amenity[] {
                            new Amenity(R.drawable.ic_user_activity, "Gym boutique", "Rutinas privadas con equipamiento compacto y acabados sobrios."),
                            new Amenity(R.drawable.ic_user_explore, "Sky terrace", "Terraza abierta con comedor exterior y vistas de barrio parque."),
                            new Amenity(R.drawable.ic_user_chat, "Business room", "Salas discretas para reuniones cortas y trabajo tranquilo."),
                            new Amenity(R.drawable.ic_user_shield, "Acceso monitorizado", "Lobby privado, camaras y ingreso digital asistido.")
                    }
            );
        }
        return null;
    }

    public static PropertyDetail findByTitle(String title) {
        if (title == null) {
            return null;
        }
        String normalized = title.trim().toLowerCase(Locale.US);
        for (PropertyDetail item : getAllProperties()) {
            if (item.getTitle().trim().toLowerCase(Locale.US).equals(normalized)) {
                return item;
            }
        }
        return null;
    }

    private static List<PropertyDetail> getAllProperties() {
        List<PropertyDetail> items = new ArrayList<>();
        items.add(getById(ID_VILLA_LUMINARA));
        items.add(getById(ID_IRON_WORKS));
        items.add(getById(ID_REFUGIO_CELESTE));
        items.add(getById(ID_CASA_MERIDIAN));
        items.add(getById(ID_ATICO_DEL_PARQUE));
        return items;
    }
}
