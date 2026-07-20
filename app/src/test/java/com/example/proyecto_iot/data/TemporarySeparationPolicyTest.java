package com.example.proyecto_iot.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TemporarySeparationPolicyTest {

    @Test
    public void temporaryLockOnlyBlocksUntilItsServerExpiration() {
        assertTrue(TemporarySeparationPolicy.isBlockingLock("RETENIDA_TEMPORALMENTE", 2_000L, 1_000L));
        assertFalse(TemporarySeparationPolicy.isBlockingLock("RETENIDA_TEMPORALMENTE", 1_000L, 1_000L));
    }

    @Test
    public void verifiedAndConfirmedLocksRemainUnavailable() {
        assertTrue(TemporarySeparationPolicy.isBlockingLock("PAGO_EN_VERIFICACION", 0L, Long.MAX_VALUE));
        assertTrue(TemporarySeparationPolicy.isBlockingLock("SEPARACION_CONFIRMADA", 0L, Long.MAX_VALUE));
    }

    @Test
    public void legacyStatesRemainReadable() {
        assertEquals(TemporarySeparationPolicy.CONFIRMED,
                TemporarySeparationPolicy.normalizeOperationalStatus("", "Aprobada"));
        assertEquals(TemporarySeparationPolicy.PENDING_PAYMENT,
                TemporarySeparationPolicy.normalizeOperationalStatus("", "Pendiente"));
        assertEquals("Unidad retenida temporalmente",
                TemporarySeparationPolicy.displayLabel(TemporarySeparationPolicy.PENDING_PAYMENT));
    }
}
