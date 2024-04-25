package com.alameda.lightningenergy.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChineseMedicinalMaterial {
    private String id;
    private String typeTitle;
    private int drugId;
    private String title;
    private String initial;
    private List<Content> content;
    private List<String> imgList;
    private String channelTropismType;
    private String tasteType;
    private String propertiesType;
    private String source;
    private boolean collectState;
    private boolean charge;
    private int lookNum;
    private String pinYin;
    private String dataType;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class Content{
        private String title;
        private String value;
    }

}
