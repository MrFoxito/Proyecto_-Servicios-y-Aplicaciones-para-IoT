package com.example.proyecto_iot.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FirebaseAppointmentRepositoryTest {

    @Test
    public void normalizesIsoAndLegacyDatesWithoutGuessingInvalidValues() {
        assertEquals("2026-06-20", FirebaseAppointmentRepository.normalizeDate("2026-06-20"));
        assertEquals("2026-06-20", FirebaseAppointmentRepository.normalizeDate("20/06/2026"));
        assertEquals("2026-06-20", FirebaseAppointmentRepository.normalizeDate("20 Jun 2026"));
        assertEquals("", FirebaseAppointmentRepository.normalizeDate("fecha desconocida"));
    }

    @Test
    public void normalizesLegacyAppointmentTimes() {
        assertEquals("09:00", FirebaseAppointmentRepository.normalizeTime("09_00"));
        assertEquals("15:30", FirebaseAppointmentRepository.normalizeTime("3:30 PM"));
        assertEquals("", FirebaseAppointmentRepository.normalizeTime("sin hora"));
    }
}
