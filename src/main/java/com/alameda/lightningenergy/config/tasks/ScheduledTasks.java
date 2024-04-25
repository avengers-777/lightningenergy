package com.alameda.lightningenergy.config.tasks;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tron.trident.core.ApiWrapper;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class ScheduledTasks {
    private final RateLimiterService rateLimiterService;
    private final ApiWrapper apiWrapper;
    private final RASUtils rasUtils;
    private final TronScanBlockServiceImpl tronScanBlockService;
    private final TronResourceExchangeServiceImpl tronResourceService;
    private final TronResourceRentalOrderServiceImpl resourceRentalOrderService;
    private final ResourceUsageStatisticsService resourceUsageStatisticsService;
    private static final double INITIAL_VALVE = 1.5;
    private static final double MIN_VALVE = 1.5;
    private static final double MAX_VALVE = 3.0;
    private static final double VALVE_DECREMENT = 0.5;
    private static final double SMALL_VALVE_DECREMENT = 0.05;

    private Double valve = INITIAL_VALVE;

//    @Scheduled(fixedDelay = 500)
    public void runEveryThreeSeconds() {
        Mono<Long> blockHeightMono = Mono.defer(()-> tronScanBlockService.getBlock().map(block -> block.getBlockHeader().getRawData().getNumber()-1));
        rateLimiterService.calculateTronCurrentBlockHeightDifference()
                .switchIfEmpty(Mono.just(3.0))
                .filter(difference -> tronScanBlockService.getEnableScanBlock())
                .filter(difference -> difference >= valve)
                .flatMap(difference -> rateLimiterService.getTronBlockHeight()
                        .switchIfEmpty(blockHeightMono))
                .map(height -> height +1L)
                .flatMap(height -> rateLimiterService.isAllowedBlockHeight(height).map(aBoolean ->  height))
                .doOnNext(System.out::println)
                .flatMap(height -> tronScanBlockService.getBlockDataByNumber(height)
                        .doOnError(e -> {
                            this.valve += 1;
                            rateLimiterService.clearLockedBlockHeight(height).subscribe();
                        })
                )
                .flatMap(tuple2 -> {
                    adjustValve();
                    return rateLimiterService.saveTronBlockHeight(tuple2.getT1().getBlockHeader().getRawData().getNumber())
                            .thenReturn(tuple2);
                })
                .flatMapMany(tronScanBlockService::blockDateHandler)
                .collectList()
                .subscribe(records->{

                }, Throwable::printStackTrace);

    }
    private void adjustValve() {
        if (valve > MAX_VALVE) {
            valve -= VALVE_DECREMENT;
        } else if (valve > MIN_VALVE) {
            valve -= SMALL_VALVE_DECREMENT;
        }
        System.out.println(valve);
    }
//    @Scheduled(fixedDelay = 600000)
    public void updateTronLastHeight() {
        tronScanBlockService.getBlock()
                        .subscribe(block -> {
                            long height = block.getBlockHeader().getRawData().getNumber();
                            long timestamp = block.getBlockHeader().getRawData().getTimestamp();
                            rateLimiterService.saveTronBlockHeightRecord(height,timestamp).subscribe();
                        });
    }
//    @Scheduled(fixedDelay = 1000 * 60 * 60) // 60 分钟
    public void updateEnergyAndBandwidthPrice(){
        tronResourceService.updateAccountResourceMessage();
    }

//    @Scheduled(fixedDelay = 1000 * 60)
    public void processResourceReclamation(){
        resourceRentalOrderService.processResourceReclamation().subscribe();
        resourceUsageStatisticsService.computeAndUpdateResourceDelegationCapacities();
    }




}
