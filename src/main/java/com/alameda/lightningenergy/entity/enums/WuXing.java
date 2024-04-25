package com.alameda.lightningenergy.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WuXing {
    METAL("金"),
    WOOD("木"),
    WATER("水"),
    FIRE("火"),
    EARTH("土");

    private final String name;
}
