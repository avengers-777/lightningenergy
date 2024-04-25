package com.alameda.lightningenergy.entity.data;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.tron.trident.proto.Common;

import java.time.Duration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("resource_price")
public class ResourcePrice {
    @Id
    private String id;
    private Common.ResourceCode resourceCode;
    private Long minDuration;
    private Long maxDuration;
    private Long price;


}
