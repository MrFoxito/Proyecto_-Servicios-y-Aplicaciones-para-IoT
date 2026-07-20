package com.example.proyecto_iot.usuario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ExploreProjectPresentationPolicyTest {
    @Test
    public void usesOnlyDistrictAndCityForDiscoveryLocation() {
        assertEquals("Miraflores, Lima", ExploreProjectPresentationPolicy.location("Miraflores", "Lima"));
        assertEquals("Miraflores", ExploreProjectPresentationPolicy.location("Miraflores", ""));
        assertEquals("", ExploreProjectPresentationPolicy.location(null, null));
    }

    @Test
    public void buildsSummaryFromAvailableTypologyData() {
        assertEquals("2–3 dorm · 85 m²–120 m²", ExploreProjectPresentationPolicy.typologySummary(
                Arrays.asList("2", "3"), Arrays.asList("85 m²", "120 m²")
        ));
        assertEquals("", ExploreProjectPresentationPolicy.typologySummary(
                Collections.emptyList(), Collections.emptyList()
        ));
    }

    @Test
    public void loadsOnlyOnceNearTheEndWhenPaginationIsAvailable() {
        assertTrue(ExploreProjectPresentationPolicy.shouldLoadMore(false, true, 8, 10));
        assertFalse(ExploreProjectPresentationPolicy.shouldLoadMore(true, true, 8, 10));
        assertFalse(ExploreProjectPresentationPolicy.shouldLoadMore(false, false, 8, 10));
        assertFalse(ExploreProjectPresentationPolicy.shouldLoadMore(false, true, 4, 10));
    }
}
