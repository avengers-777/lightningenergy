package com.alameda.lightningenergy.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Currency {
    TRX(6),
    TRC10(6);
    private final int decimalPlaces;

    public long calculateValue(long value) {
        return value * (long) Math.pow(10, decimalPlaces);
    }
    public double convertToBaseValue(long value) {
        return (double) value / Math.pow(10, decimalPlaces);
    }

}
