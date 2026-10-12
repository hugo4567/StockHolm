package com.stockholm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Part(
        Long id,
        Long productId,
        String name,
        String followUpText,
        BigDecimal cost,
        int quantity,
        LocalDate followUpDate,
        PartStatus status
) {
    @Override
    public String toString() {
        return (id == null ? "Nouveau" : "#" + id) + " - " + name;
    }
}
