package com.example.proyecto_iot.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class AppointmentChatSecurityTest {
    @Test
    public void appointmentConversationIdIsStableAndScopedToOneAppointment() {
        String first = FirebaseChatRepository.appointmentConversationId(
                "cliente-a", "asesor-a", "cita-a");
        assertTrue(first.startsWith("appointment_chat_"));
        assertTrue(first.equals(FirebaseChatRepository.appointmentConversationId(
                "cliente-a", "asesor-a", "cita-a")));
        assertNotEquals(first, FirebaseChatRepository.appointmentConversationId(
                "cliente-a", "asesor-a", "cita-b"));
    }

    @Test
    public void appointmentParticipantsMustMatchTheClientAndAdvisor() {
        assertTrue(FirebaseAppointmentRepository.hasConsistentParticipants(
                Arrays.asList("cliente-a", "asesor-a"), "cliente-a", "asesor-a"));
        assertFalse(FirebaseAppointmentRepository.hasConsistentParticipants(
                Arrays.asList("cliente-a", "asesor-ajeno"), "cliente-a", "asesor-a"));
        assertFalse(FirebaseAppointmentRepository.hasConsistentParticipants(
                Collections.singletonList("cliente-a"), "cliente-a", "asesor-a"));
    }
}
