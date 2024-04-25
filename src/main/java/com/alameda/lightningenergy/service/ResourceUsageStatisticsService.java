package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.entity.app.ResourceConsumptionRecord;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronResourceRentalOrder;
import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.utils.BlockUtils;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceUsageStatisticsService {
    private final TronResourceRentalOrderServiceImpl resourceRentalOrderService;
    private final TronAccountServiceImpl tronAccountService;
    private final TronTransactionsServiceImpl tronTransactionsService;
    private final TronResourceExchangeServiceImpl resourceExchangeService;

    public void computeAndUpdateResourceDelegationCapacities(){
        Flux<Tuple2<TronAccount ,ResourceConsumptionRecord>> allTronAccountResourceUsage = tronAccountService.findAll()
                .flatMap(tronAccount -> compute24HourAccountResourceConsumption(tronAccount.getId()).map(record -> Tuples.of(tronAccount,record))).share();
        allTronAccountResourceUsage.filter(tuple2 -> !tuple2.getT1().getAccountType().equals(AccountType.AUTHORIZED))
                .flatMap(this::updateAccountDelegationCapacities).subscribe();
        allTronAccountResourceUsage.filter(tuple2 -> tuple2.getT1().getAccountType().equals(AccountType.AUTHORIZED))
                .flatMap(this::calculateAndUpdateAccountResourceUsage)
                .flatMap(this::updateAccountDelegationCapacities).subscribe();
    }
    public Mono<Tuple2<TronAccount ,ResourceConsumptionRecord>> calculateAndUpdateAccountResourceUsage(Tuple2<TronAccount ,ResourceConsumptionRecord> tuple2){
        ResourceConsumptionRecord initialRecord = new ResourceConsumptionRecord(); // 新实例，避免副作用
        return resourceRentalOrderService.findRentalOrdersInLast24Hours(tuple2.getT1().getId())
                .flatMap(this::computeResourceConsumptionForOrder)
                .reduce(initialRecord,(record, record2) -> {
                    record.addEnergy(record2.getEnergyConsumed());
                    record.addBandwidth(record2.getBandwidthConsumed());
                    return record;
                })
                .map(record -> {
                    record.addEnergy(tuple2.getT2().getEnergyConsumed());
                    record.addBandwidth(tuple2.getT2().getBandwidthConsumed());
                    return Tuples.of(tuple2.getT1(),record);
                })
                .defaultIfEmpty(tuple2);

    };
    public Mono<ResourceConsumptionRecord> computeResourceConsumptionForOrder(TronResourceRentalOrder order){
        return tronTransactionsService.findTransactionsInTimeRange(order.getReceivingAccountId(), List.of(order.getTransactionTime(),order.getCompleteTime()))
                .reduce(new ResourceConsumptionRecord(),(record,transaction)->{
                    record.addEnergy(BlockUtils.calculateResourceCurrentConsumption(transaction.getEnergyUsage(),transaction.getBlockTimeStamp()));
                    record.addBandwidth(BlockUtils.calculateResourceCurrentConsumption(transaction.getNetUsage(),transaction.getBlockTimeStamp()));
                    return record;
                })
                .map(record -> record.updateMaxUsage(order));
    }

    public Mono<UpdateResult> updateAccountDelegationCapacities(Tuple2<TronAccount ,ResourceConsumptionRecord> tuple2){
        ResourceConsumptionRecord updatedRecord =  tuple2.getT2().convertedTrx(resourceExchangeService);
        tuple2.getT1().getBalance().updateAvailableDelegationCapacities(updatedRecord);
        return tronAccountService.updateCanDelegatedResource(tuple2.getT1().getId(),tuple2.getT1().getBalance().getCanDelegatedForEnergy(),tuple2.getT1().getBalance().getCanDelegatedForBandwidth());
    }
    public Mono<ResourceConsumptionRecord> compute24HourAccountResourceConsumption(String tronAccountId){
        return tronTransactionsService.findTransactionsInTimeRange(tronAccountId, List.of(System.currentTimeMillis() - Duration.ofDays(1).toMillis(),System.currentTimeMillis()))
                .reduce(new ResourceConsumptionRecord(),(record,transaction)->{
                    record.addEnergy(BlockUtils.calculateResourceCurrentConsumption(transaction.getEnergyUsage(),transaction.getBlockTimeStamp()));
                    record.addBandwidth(BlockUtils.calculateResourceCurrentConsumption(transaction.getNetUsage(),transaction.getBlockTimeStamp()));
                    return record;
                });
    }
}
