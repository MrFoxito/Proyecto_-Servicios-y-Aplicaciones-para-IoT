package com.example.proyecto_iot.admin.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AdminProjectFormTypologyItemTest {
    @Test
    public void convertsLegacyUsdAmountsToPen() {
        AdminProjectFormTypologyItem item = new AdminProjectFormTypologyItem(
                "Tipo A", true, "70 m2", "2 habs", "2 banos", "350000 USD", "1500 USD"
        );

        assertEquals(1_295_000d, item.getTotalAmountValue(), 0.001d);
        assertEquals(5_550d, item.getSeparationAmountValue(), 0.001d);
        assertEquals("S/ 1295000", item.getTotalAmount());
    }

    @Test
    public void preservesNewPenAmounts() {
        AdminProjectFormTypologyItem item = new AdminProjectFormTypologyItem(
                "Tipo A", true, "70 m2", "2 habs", "2 banos", "350000", "1500"
        );

        assertEquals(350_000d, item.getTotalAmountValue(), 0.001d);
        assertEquals("S/ 350000", AdminProjectFormTypologyItem.formatAsPen("350000"));
        assertEquals("Precio por definir", AdminProjectFormTypologyItem.formatAsPen("Precio por definir"));
    }
}
