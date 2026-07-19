package com.example.proyecto_iot.usuario;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProjectQrParserTest {
    @Test
    public void extractsProjectIdFromValidProjectQr() {
        assertEquals("project-123", ProjectQrParser.extractProjectId("app://proyecto/project-123"));
    }

    @Test
    public void rejectsUnknownSchemeOrHost() {
        assertEquals("", ProjectQrParser.extractProjectId("https://proyecto/project-123"));
        assertEquals("", ProjectQrParser.extractProjectId("app://cliente/client-123"));
    }

    @Test
    public void rejectsMissingOrCompoundProjectId() {
        assertEquals("", ProjectQrParser.extractProjectId("app://proyecto"));
        assertEquals("", ProjectQrParser.extractProjectId("app://proyecto/"));
        assertEquals("", ProjectQrParser.extractProjectId("app://proyecto/one/two"));
    }

    @Test
    public void rejectsFreeTextAndExtraUrlParts() {
        assertEquals("", ProjectQrParser.extractProjectId("Proyecto del mes"));
        assertEquals("", ProjectQrParser.extractProjectId("app://proyecto/project-123?source=qr"));
    }
}
