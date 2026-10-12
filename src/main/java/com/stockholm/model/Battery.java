package com.stockholm.model;

public record Battery(
        Long id,
        String model,
        int cellCount,
        double voltage,
        double capacityAh,
        String format,
        String notes
) {
    @Override
    public String toString() {
        return (id == null ? "Nouvelle" : "#" + id) + " - " + model;
    }
}
