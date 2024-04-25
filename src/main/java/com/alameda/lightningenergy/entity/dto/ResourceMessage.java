package com.alameda.lightningenergy.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tron.trident.proto.Response;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceMessage {
    private Long totalEnergyWeight;
    private Long totalEnergyLimit;
    private Long totalNetWeight;
    private Long totalNetLimit;

    public ResourceMessage(Response.AccountResourceMessage accountResourceMessage){
        this.totalEnergyWeight = accountResourceMessage.getTotalEnergyWeight();
        this.totalEnergyLimit = accountResourceMessage.getTotalEnergyLimit();
        this.totalNetWeight = accountResourceMessage.getTotalNetWeight();
        this.totalNetLimit = accountResourceMessage.getTotalNetLimit();
    }
}
