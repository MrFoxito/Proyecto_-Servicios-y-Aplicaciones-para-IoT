package com.example.proyecto_iot.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FirebaseReportRepositoryTest {
    @Test
    public void normalizesUsdToPenWithTheConfiguredRate() {
        assertEquals(370d, FirebaseReportRepository.normalizeAmountToPen(100d, "USD"), 0.001d);
    }

    @Test
    public void preservesPenAmountsAndTreatsLegacyCurrencyAsUsd() {
        assertEquals(100d, FirebaseReportRepository.normalizeAmountToPen(100d, "PEN"), 0.001d);
        assertEquals(370d, FirebaseReportRepository.normalizeAmountToPen(100d, ""), 0.001d);
    }

    @Test
    public void countsOnlyApprovedOrPaidSeparationsAsConfirmedAmounts() {
        assertEquals(true, FirebaseReportRepository.countsAsApprovedAmount("Aprobada", ""));
        assertEquals(true, FirebaseReportRepository.countsAsApprovedAmount("Pagada", ""));
        assertEquals(true, FirebaseReportRepository.countsAsApprovedAmount("Pendiente", "processed"));
        assertEquals(false, FirebaseReportRepository.countsAsApprovedAmount("Pendiente", ""));
        assertEquals(false, FirebaseReportRepository.countsAsApprovedAmount("Rechazada", ""));
    }
}
