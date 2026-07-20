package com.example.proyecto_iot.usuario;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ProjectSearchPolicyTest {

    @Test
    public void matchesIgnoresCaseAccentsAndEnye() {
        String searchable = "Departamento con 2 baños en Barranco";

        assertTrue(ProjectSearchPolicy.matches(searchable, "BAÑO"));
        assertTrue(ProjectSearchPolicy.matches(searchable, "bano"));
        assertTrue(ProjectSearchPolicy.matches(searchable, "bañ"));
    }

    @Test
    public void matchesPartialTextAcrossPublicProjectFields() {
        String searchable = "Edificio Aurora Vista al mar Piscina Gimnasio 3 habitaciones";

        assertTrue(ProjectSearchPolicy.matches(searchable, "vist"));
        assertTrue(ProjectSearchPolicy.matches(searchable, "gimna"));
        assertTrue(ProjectSearchPolicy.matches(searchable, "habit"));
    }

    @Test
    public void rejectsEmptyAndUnmatchedQueries() {
        String searchable = "Proyecto Central con terraza";

        assertFalse(ProjectSearchPolicy.matches(searchable, ""));
        assertFalse(ProjectSearchPolicy.matches(searchable, "cochera"));
    }

    @Test
    public void limitsSuggestionsWithoutChangingTheirOrder() {
        List<String> values = Arrays.asList("Proyecto Norte", "Proyecto Centro", "Proyecto Sur");

        List<String> results = ProjectSearchPolicy.filter(values, "proyecto", 2, value -> value);

        org.junit.Assert.assertEquals(Arrays.asList("Proyecto Norte", "Proyecto Centro"), results);
    }
}
