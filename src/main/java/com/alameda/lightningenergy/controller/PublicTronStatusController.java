package com.alameda.lightningenergy.controller;

import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.service.RateLimiterService;
import com.alameda.lightningenergy.service.TronScanBlockServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/g/v1/status/tron")
public class PublicTronStatusController {
    private final TronScanBlockServiceImpl tronScanBlockService;
    private final RateLimiterService rateLimiterService;


    @GetMapping("/scan-block")
    public Mono<ResModel<Boolean>> fetchScanBlockEnabledStatus(){
        return Mono.just(ResModel.success(tronScanBlockService.getEnableScanBlock()));
    }

    @GetMapping("/scan-block-height")
    public Mono<ResModel<Long>> fetchCurrentTronBlockHeight(){
        return  rateLimiterService.getTronBlockHeight().map(ResModel::success);
    }
    @GetMapping("/historical-block-data")
    public Mono<ResModel<Tuple2<Long, Long>>> fetchBlockHeightAndTimestampHistory(){
        return  rateLimiterService.getBlockHeightAndUpdateTime().map(ResModel::success);
    }

}
