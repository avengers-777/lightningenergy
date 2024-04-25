package com.alameda.lightningenergy.entity.app;

import com.alameda.lightningenergy.entity.data.TronResourceRentalOrder;
import com.alameda.lightningenergy.service.TronResourceExchangeServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.tron.trident.proto.Common;

@Data
@AllArgsConstructor
public class ResourceConsumptionRecord {
    private long energyConsumed;
    private long bandwidthConsumed;

    public ResourceConsumptionRecord(){
        this.energyConsumed = 0L;
        this.bandwidthConsumed = 0L;
    }
    public void addEnergy(long amount){
        this.energyConsumed += amount;
    }
    public void addBandwidth(long amount){
        this.bandwidthConsumed += amount;
    }
    public ResourceConsumptionRecord updateMaxUsage(TronResourceRentalOrder order){
        switch (order.getResourceCode()){
            case ENERGY -> {
                this.energyConsumed = Math.min(order.getAmount(),energyConsumed);
            }
            case BANDWIDTH -> {
                this.bandwidthConsumed = Math.min(order.getAmount(),bandwidthConsumed);
            }
        }
        return this;

    }
    public ResourceConsumptionRecord convertedTrx(TronResourceExchangeServiceImpl resourceExchangeService){
        this.energyConsumed = resourceExchangeService.energyToTrx(this.energyConsumed);
        this.bandwidthConsumed = resourceExchangeService.bandwidthToTrx(this.bandwidthConsumed);
        return this;
    }
}
