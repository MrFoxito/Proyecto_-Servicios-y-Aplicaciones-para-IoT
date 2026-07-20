package com.example.proyecto_iot.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CompanyNamePolicyTest {
    @Test
    public void companyNameHasPriorityOverLegacyValues() {
        assertEquals("Editorial Estate", CompanyNamePolicy.resolve(
                "Editorial Estate", "Alias empresa", "Alias usuario", "Alias antiguo", "InvitaciÃ³n"));
    }

    @Test
    public void userAndInvitationValuesRecoverLegacyRecords() {
        assertEquals("Inmobiliaria Norte", CompanyNamePolicy.resolve(
                "", "", "Inmobiliaria Norte", "", "InvitaciÃ³n"));
        assertEquals("Inmobiliaria Sur", CompanyNamePolicy.resolve(
                "", "", "", "", "Inmobiliaria Sur"));
    }

    @Test
    public void pendingInvitationComparisonIgnoresCaseAccentsAndSpacing() {
        assertTrue(CompanyNamePolicy.matches("  Inmobiliaria Águila  ", "inmobiliaria aguila"));
        assertFalse(CompanyNamePolicy.matches("Inmobiliaria Águila", "Inmobiliaria Norte"));
    }

    @Test
    public void onlyEmptyValuesAreEligibleForRepair() {
        assertTrue(CompanyNamePolicy.shouldBackfill("", "Editorial Estate"));
        assertFalse(CompanyNamePolicy.shouldBackfill("Nombre existente", "Editorial Estate"));
    }
}
