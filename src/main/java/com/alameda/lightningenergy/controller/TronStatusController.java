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
@RequestMapping("/a/v1/pri/status/tron")
public class TronStatusController {
    private final TronScanBlockServiceImpl tronScanBlockService;
    private final RateLimiterService rateLimiterService;



    @PatchMapping("/scan-block")
    public Mono<ResModel<Boolean>> changeScanBlockEnabledStatus(@RequestParam("enabled") boolean enabled ){
        tronScanBlockService.setEnableScanBlock(enabled);
        return Mono.just(ResModel.success(tronScanBlockService.getEnableScanBlock()));
    }
    @PatchMapping("/init-scan-block-height")
    public Mono<ResModel<Object>> initScanBlockHeight(){
        tronScanBlockService.initScanBlockHeight();
        return Mono.just(ResModel.success());
    }


}
