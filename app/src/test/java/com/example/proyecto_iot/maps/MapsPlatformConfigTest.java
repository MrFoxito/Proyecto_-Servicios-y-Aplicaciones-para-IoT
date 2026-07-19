package com.example.proyecto_iot.maps;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MapsPlatformConfigTest {
    @Test
    public void acceptsOnlyGoogleAndroidApiKeys() {
        assertTrue(MapsPlatformConfig.isApiKey("AIzaSyD4W2X123456789012345678901234567"));
        assertFalse(MapsPlatformConfig.isApiKey(""));
        assertFalse(MapsPlatformConfig.isApiKey("DEFAULT_API_KEY"));
        assertFalse(MapsPlatformConfig.isApiKey("AIzaREEMPLAZA_CON_TU_CLAVE_ANDROID"));
        assertFalse(MapsPlatformConfig.isApiKey("invalid-public-token"));
    }
}
