package com.example.proyecto_iot.admin.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AdminProjectFormTypologyItem {
    private final String title;
    private final boolean available;
    private final String area;
    private final String bedrooms;
    private final String bathrooms;
    private final double totalAmount;
    private final double separationAmount;

    public AdminProjectFormTypologyItem(
            String title,
            boolean available,
            String area,
            String bedrooms,
            String bathrooms,
            String totalAmount,
            String separationAmount
    ) {
        this(title, available, area, bedrooms, bathrooms, parseAmount(totalAmount), parseAmount(separationAmount));
    }

    public AdminProjectFormTypologyItem(
            String title,
            boolean available,
            String area,
            String bedrooms,
            String bathrooms,
            double totalAmount,
            double separationAmount
    ) {
        this.title = title;
        this.available = available;
        this.area = area;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.totalAmount = totalAmount;
        this.separationAmount = separationAmount;
    }

    public AdminProjectFormTypologyItem(
            String title,
            boolean available,
            String area,
            String bedrooms,
            String totalAmount,
            String separationAmount
    ) {
        this(title, available, area, bedrooms, "2 banos", totalAmount, separationAmount);
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getStatusLabel() {
        return available ? "DISPONIBLE" : "NO DISPONIBLE";
    }

    public String getArea() {
        return area;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public String getTotalAmount() {
        return formatUsd(totalAmount);
    }

    public String getSeparationAmount() {
        return formatUsd(separationAmount);
    }

    public double getTotalAmountValue() {
        return totalAmount;
    }

    public double getSeparationAmountValue() {
        return separationAmount;
    }

    public String getTotalAmountInputValue() {
        return formatPlain(totalAmount);
    }

    public String getSeparationAmountInputValue() {
        return formatPlain(separationAmount);
    }

    private static double parseAmount(String rawAmount) {
        if (rawAmount == null) {
            return 0d;
        }
        String normalized = rawAmount.replace(",", "")
                .replaceAll("[^0-9.]", "");
        int firstDot = normalized.indexOf('.');
        if (firstDot >= 0) {
            normalized = normalized.substring(0, firstDot + 1)
                    + normalized.substring(firstDot + 1).replace(".", "");
        }
        if (normalized.isEmpty() || ".".equals(normalized)) {
            return 0d;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return 0d;
        }
    }

    private static String formatUsd(double amount) {
        return formatPlain(amount) + " USD";
    }

    private static String formatPlain(double amount) {
        DecimalFormat formatter = new DecimalFormat(
                amount == Math.rint(amount) ? "0" : "0.##",
                DecimalFormatSymbols.getInstance(Locale.US)
        );
        formatter.setGroupingUsed(false);
        return formatter.format(amount);
    }
}
