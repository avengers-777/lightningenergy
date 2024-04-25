package com.alameda.lightningenergy.entity.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResV2 {
    private int code;
    private String message;
    private DataBody data;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataBody {
        private int total;
        private boolean hasNextPage;
        private List<ChineseMedicinalMaterial> list;
    }
}
