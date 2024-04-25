package com.alameda.lightningenergy.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TianGan {
    @JsonProperty("甲")
    JIA("甲", 4, WuXing.WOOD),
    @JsonProperty("乙")
    YI("乙", 5, WuXing.WOOD),
    @JsonProperty("丙")
    BING("丙", 6, WuXing.FIRE),
    @JsonProperty("丁")
    DING("丁", 7, WuXing.FIRE),
    @JsonProperty("戊")
    WU("戊", 8, WuXing.EARTH),
    @JsonProperty("己")
    JI("己", 9, WuXing.EARTH),
    @JsonProperty("庚")
    GENG("庚", 0, WuXing.METAL),
    @JsonProperty("辛")
    XIN("辛", 1, WuXing.METAL),
    @JsonProperty("壬")
    REN("壬", 2, WuXing.WATER),
    @JsonProperty("癸")
    GUI("癸", 3, WuXing.WATER);

    private final String name;
    private final int number;
    private final WuXing wuXing;
    public static TianGan fromNumber(int number) {
        for (TianGan tianGan : TianGan.values()) {
            if (tianGan.number == number) {
                return tianGan;
            }
        }
        throw new IllegalArgumentException("Invalid number for TianGan: " + number);
    }
}