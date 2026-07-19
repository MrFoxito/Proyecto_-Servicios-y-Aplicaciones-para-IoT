package com.example.proyecto_iot.usuario;

import java.net.URI;
import java.net.URISyntaxException;

/** Valida el unico formato QR admitido por el flujo de exploracion. */
public final class ProjectQrParser {
    private static final String SCHEME = "app";
    private static final String HOST = "proyecto";

    private ProjectQrParser() {
    }

    /**
     * Devuelve el ID del documento cuando el valor coincide exactamente con
     * app://proyecto/{projectId}; devuelve vacio ante cualquier otro valor.
     */
    public static String extractProjectId(String scannedValue) {
        if (scannedValue == null || scannedValue.trim().isEmpty()) {
            return "";
        }

        try {
            URI uri = new URI(scannedValue.trim());
            if (!SCHEME.equalsIgnoreCase(uri.getScheme())
                    || !HOST.equalsIgnoreCase(uri.getHost())
                    || uri.getPort() != -1
                    || uri.getUserInfo() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null) {
                return "";
            }

            String path = uri.getRawPath();
            if (path == null || !path.matches("/[^/]+")) {
                return "";
            }

            String projectId = path.substring(1);
            return projectId.trim().isEmpty() ? "" : projectId;
        } catch (URISyntaxException ignored) {
            return "";
        }
    }
}
