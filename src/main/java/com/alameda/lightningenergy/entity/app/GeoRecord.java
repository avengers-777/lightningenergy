package com.alameda.lightningenergy.entity.app;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeoRecord {
    private Integer id;
    private Integer pid;
    private Integer deep;
    private String name;
    private List<String> extPath;
    private Double latitude;
    private Double longitude;
    public GeoRecord(int id, int pid, int deep, String name, List<String> extPath, String geo) {
        this.id = id;
        this.pid = pid;
        this.deep = deep;
        this.name = name;
        this.extPath = extPath;

        String[] latLong = geo.split(" ");
        if (latLong.length == 2) {
            this.latitude = Double.parseDouble(latLong[1]);
            this.longitude = Double.parseDouble(latLong[0]);
        }
    }
}
