package com.example.proyecto_iot.maps;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MapboxConfigTest {
    @Test
    public void acceptsOnlyPublicMapboxTokens() {
        assertTrue(MapboxConfig.isPublicToken("pk.eyJ1IjoidGVzdCIsImEiOiJjbXNhbXBsZSJ9.signature"));
        assertFalse(MapboxConfig.isPublicToken(""));
        assertFalse(MapboxConfig.isPublicToken("YOUR_MAPBOX_ACCESS_TOKEN"));
        assertFalse(MapboxConfig.isPublicToken("sk.secret-token"));
    }
}
