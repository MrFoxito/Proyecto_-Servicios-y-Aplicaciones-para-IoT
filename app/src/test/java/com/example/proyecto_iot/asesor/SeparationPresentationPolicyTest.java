package com.example.proyecto_iot.asesor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.proyecto_iot.entity.Separacion;

import org.junit.Test;

public class SeparationPresentationPolicyTest {
    @Test public void resolvesCanonicalProjectIdWithLegacyAliases() {
        assertEquals("property", SeparationPresentationPolicy.canonicalProjectId("property", "project", "legacy"));
        assertEquals("project", SeparationPresentationPolicy.canonicalProjectId(" ", "project", "legacy"));
        assertEquals("legacy", SeparationPresentationPolicy.canonicalProjectId(null, "", "legacy"));
    }

    @Test public void selectsTheFirstUsableProjectImageThenGallery() {
        assertEquals("https://cdn.example.com/main.jpg", SeparationProjectImagePolicy.select(
                "https://cdn.example.com/main.jpg", "https://cdn.example.com/secondary.jpg", "", "", "https://cdn.example.com/gallery.jpg"));
        assertEquals("https://cdn.example.com/gallery.jpg", SeparationProjectImagePolicy.select(
                "bad-url", "", "", "", "https://cdn.example.com/gallery.jpg"));
        assertEquals("", SeparationProjectImagePolicy.select(null, "", "not-a-url", "", null));
    }

    @Test public void includesOnlyPaidOrApprovedPenAmountsInManagedKpi() {
        Separacion pen = separation("Aprobada", "PEN", "S/ 900.00", 900d);
        Separacion historicPen = separation("Pagada", null, "S/ 1,250.50", 0d);
        Separacion usd = separation("Pagada", "USD", "$ 500.00", 500d);
        Separacion pending = separation("Pendiente", "PEN", "S/ 250.00", 250d);

        assertTrue(SeparationPresentationPolicy.isManagedStatus(pen));
        assertTrue(SeparationPresentationPolicy.isManagedPen(historicPen));
        assertEquals(900d, SeparationPresentationPolicy.penAmount(pen), 0d);
        assertEquals(1250.50d, SeparationPresentationPolicy.penAmount(historicPen), 0d);
        assertFalse(SeparationPresentationPolicy.isManagedPen(usd));
        assertFalse(SeparationPresentationPolicy.isManagedStatus(pending));
    }

    @Test public void formatsOnlyValidPricingAndNeverRendersCurrencyWithoutAnAmount() {
        assertEquals("S/ 50,000.00", SeparationPricingPolicy.formatPen(50000d));
        assertEquals("Monto no configurado", SeparationPricingPolicy.display("S/", 0d, "S/"));
        assertEquals("S/ 1,250.00", SeparationPricingPolicy.display("", 1250d, ""));
        assertEquals(16668518.5d, SeparationPricingPolicy.number("S/ 16668518.5"), 0d);
    }

    private Separacion separation(String status, String currency, String monto, double amount) {
        Separacion separation = new Separacion();
        separation.setEstado(status);
        separation.setCurrency(currency);
        separation.setMontoTexto(monto);
        separation.setAmount(amount);
        return separation;
    }
}
