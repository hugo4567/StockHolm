package com.stockholm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Product(
        Long id,
        String name,
        String purchasePlatform,
        BigDecimal purchasePrice,
        BigDecimal shippingCost,
        LocalDate purchaseDate,
        String initialDiagnosis,
        int timeSpentMinutes,
        BigDecimal partsCost,
        ProductStatus status,
        String saleChannel,
        BigDecimal salePrice,
        BigDecimal saleFee
) {
    @Override
    public String toString() {
        return (id == null ? "Nouveau" : "#" + id) + " - " + name;
    }
}
