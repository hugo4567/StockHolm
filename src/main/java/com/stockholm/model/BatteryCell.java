package com.stockholm.model;

public record BatteryCell(
        Long id,
        Long batteryId,
        int cellNumber,
        BatteryCellState state
) {
}
