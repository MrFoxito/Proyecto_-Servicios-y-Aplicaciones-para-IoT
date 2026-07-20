package com.example.proyecto_iot.usuario;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class RecentProjectSearchPolicyTest {
    @Test
    public void putsTheLatestQueryFirstAndDeduplicatesIgnoringAccents() {
        assertEquals(Arrays.asList("Barranco", "Miraflores"), RecentProjectSearchPolicy.add(
                Arrays.asList("Miraflores", "barránco"), "Barranco"
        ));
    }

    @Test
    public void keepsOnlyFiveCleanQueries() {
        assertEquals(Arrays.asList("seis", "uno", "dos", "tres", "cuatro"), RecentProjectSearchPolicy.add(
                Arrays.asList("uno", "dos", "tres", "cuatro", "cinco"), "  seis  "
        ));
    }

    @Test
    public void ignoresEmptyQueries() {
        assertEquals(Collections.singletonList("Lima"), RecentProjectSearchPolicy.add(
                Collections.singletonList("Lima"), "   "
        ));
    }
}
