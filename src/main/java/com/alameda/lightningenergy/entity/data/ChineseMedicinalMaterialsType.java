package com.alameda.lightningenergy.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChineseMedicinalMaterialsType {
    private String title;
    private String type;
    private List<ChineseMedicinalMaterialsType> list;

}
