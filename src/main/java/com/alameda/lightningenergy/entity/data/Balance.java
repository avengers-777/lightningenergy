package com.alameda.lightningenergy.entity.data;

import com.alameda.lightningenergy.entity.app.ResourceConsumptionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class Balance {
    private Long amount;
    private Long energy;
    private Long bandwidth;
    private Long delegatedForEnergy;
    private Long acquiredDelegatedForEnergy;
    private Long delegatedForBandwidth;
    private Long acquiredDelegatedForBandwidth;
    private Long canDelegatedForEnergy;
    private Long canDelegatedForBandwidth;
    private Long canDelegatedForTronPower;
    private Long tronPower;
    private Set<UnstakingResource> unstakingResources;
    public static final String ENERGY_PROPERTIES = "balance.energy";
    public static final String BANDWIDTH_PROPERTIES = "balance.bandwidth";
    public static final String TRON_POWER_PROPERTIES = "balance.tronPower";
    public static final String AMOUNT_PROPERTIES = "balance.amount";
    public static final String UNSTAKING_RESOURCES_PROPERTIES = "balance.unstakingResources";
    public static final String CAN_DELEGATED_FOR_ENERGY_PROPERTIES = "balance.canDelegatedForEnergy";
    public static final String CAN_DELEGATED_FOR_BANDWIDTH_PROPERTIES = "balance.canDelegatedForBandwidth";
    public static final String ACQUIRED_DELEGATED_FOR_ENERGY_PROPERTIES = "balance.acquiredDelegatedForEnergy";
    public static  final String ACQUIRED_DELEGATED_FOR_BANDWIDTH_PROPERTIES = "balance.acquiredDelegatedForBandwidth";
    public static final String DELEGATED_FOR_ENERGY_PROPERTIES = "balance.delegatedForEnergy";
    public static final String DELEGATED_FOR_BANDWIDTH_PROPERTIES = "balance.delegatedForBandwidth";
    public Balance(){
        this.amount = 0L;
        this.unstakingResources = new HashSet<>();
        this.energy = 0L;
        this.bandwidth = 0L;
        this.delegatedForEnergy = 0L;
        this.acquiredDelegatedForEnergy = 0L;
        this.delegatedForBandwidth = 0L;
        this.acquiredDelegatedForBandwidth = 0L;
        this.tronPower = 0L;
        this.canDelegatedForBandwidth = 0L;
        this.canDelegatedForEnergy = 0L;
        this.canDelegatedForTronPower = 0L;
    }

    public void  updateAvailableDelegationCapacities(ResourceConsumptionRecord record){
        long energyDelegationDelta = acquiredDelegatedForEnergy - record.getEnergyConsumed();
        long bandwidthDelegationDelta = acquiredDelegatedForBandwidth -record.getBandwidthConsumed();
        this.canDelegatedForEnergy = energy - delegatedForEnergy + Math.min(0,energyDelegationDelta);
        this.canDelegatedForBandwidth = bandwidth - delegatedForBandwidth + Math.min(0,bandwidthDelegationDelta);
    }

    public static String getDelegatedResourceProperties(Common.ResourceCode resourceCode){
        return resourceCode.equals(Common.ResourceCode.ENERGY) ? DELEGATED_FOR_ENERGY_PROPERTIES : DELEGATED_FOR_BANDWIDTH_PROPERTIES;
    }
    public static String getAcquiredDelegatedResourceProperties(Common.ResourceCode resourceCode){
        return resourceCode.equals(Common.ResourceCode.ENERGY) ? ACQUIRED_DELEGATED_FOR_ENERGY_PROPERTIES : ACQUIRED_DELEGATED_FOR_BANDWIDTH_PROPERTIES;
    }
    public static String getResourceProperties(Common.ResourceCode resourceCode){
        return   resourceCode.equals(Common.ResourceCode.ENERGY) ? ENERGY_PROPERTIES : resourceCode.equals(Common.ResourceCode.BANDWIDTH) ? BANDWIDTH_PROPERTIES : TRON_POWER_PROPERTIES;
    }
    public static String getCanDelegatedResourceProperties(Common.ResourceCode resourceCode){
        return resourceCode.equals(Common.ResourceCode.ENERGY) ? CAN_DELEGATED_FOR_ENERGY_PROPERTIES : resourceCode.equals(Common.ResourceCode.BANDWIDTH) ? CAN_DELEGATED_FOR_BANDWIDTH_PROPERTIES : "balance.canDelegatedForTronPower";
    }
    public static Set<UnstakingResource> unfrozenV2Handler(List<Response.Account.UnFreezeV2> unFreezeV2s,long blockHeightRecord, long updateTimeRecord){
        return unFreezeV2s.stream().map(unFreezeV2 -> new UnstakingResource(unFreezeV2,blockHeightRecord,updateTimeRecord))
                .collect(Collectors.toSet());
    }

    public void updateResource(ApiWrapper apiWrapper, String address,long blockHeightRecord, long updateTimeRecord){
        Response.Account account = apiWrapper.getAccount(address);
        this.unstakingResources = unfrozenV2Handler(account.getUnfrozenV2List(),blockHeightRecord,updateTimeRecord);
        long canDelegatedForBandwidth = apiWrapper.getCanDelegatedMaxSize(address,Common.ResourceCode.BANDWIDTH.getNumber());
        long canDelegatedForEnergy = apiWrapper.getCanDelegatedMaxSize(address,Common.ResourceCode.ENERGY.getNumber());
        setCanDelegatedForBandwidth(canDelegatedForBandwidth);
        setCanDelegatedForEnergy(canDelegatedForEnergy);
        setAmount(account.getBalance());
        setDelegatedForBandwidth(account.getDelegatedFrozenV2BalanceForBandwidth());
        setDelegatedForEnergy(account.getAccountResource().getDelegatedFrozenV2BalanceForEnergy());
        setAcquiredDelegatedForBandwidth(account.getAcquiredDelegatedFrozenV2BalanceForBandwidth());
        setAcquiredDelegatedForEnergy(account.getAccountResource().getAcquiredDelegatedFrozenV2BalanceForEnergy());
        account.getFrozenV2List().forEach(frozen -> {
            switch (frozen.getType()) {
                case Common.ResourceCode.BANDWIDTH:
                    setBandwidth(frozen.getAmount() + account.getDelegatedFrozenV2BalanceForBandwidth());
                    break;
                case Common.ResourceCode.ENERGY:
                    setEnergy(frozen.getAmount() + account.getAccountResource().getDelegatedFrozenV2BalanceForEnergy());
                    break;
                case Common.ResourceCode.TRON_POWER:
                    setTronPower(frozen.getAmount());
                    break;
                default:
                    break;
            }
        });
    }


}
