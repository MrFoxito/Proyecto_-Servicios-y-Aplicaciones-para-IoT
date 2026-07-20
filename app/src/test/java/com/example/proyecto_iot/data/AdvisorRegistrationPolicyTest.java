package com.example.proyecto_iot.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdvisorRegistrationPolicyTest {
    @Test
    public void advisorRegistrationStartsPending() {
        assertEquals("pendiente", AdvisorRegistrationPolicy.initialStatus("asesor"));
        assertEquals("activo", AdvisorRegistrationPolicy.initialStatus("cliente"));
    }

    @Test
    public void pendingAndRejectedAdvisorAreBlocked() {
        assertTrue(AdvisorRegistrationPolicy.blocksAdvisorAccess("asesor", "pendiente"));
        assertTrue(AdvisorRegistrationPolicy.blocksAdvisorAccess("asesor", "rechazado"));
        assertFalse(AdvisorRegistrationPolicy.blocksAdvisorAccess("asesor", "activo"));
    }

    @Test
    public void legacyAdvisorWithoutStatusRemainsAvailable() {
        assertTrue(AdvisorRegistrationPolicy.isActiveAdvisor("asesor", ""));
        assertFalse(AdvisorRegistrationPolicy.isActiveAdvisor("cliente", "activo"));
    }
}
