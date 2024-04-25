package com.alameda.lightningenergy.controller;

import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.data.DepositOrder;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronResourceRentalOrder;
import com.alameda.lightningenergy.entity.dto.ResourceMessage;
import com.alameda.lightningenergy.entity.dto.TronResourceRentalRequest;
import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.service.*;
import com.alameda.lightningenergy.utils.IpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.tron.trident.proto.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/g/v1/tron/resource")
public class PublicTronResourceController {
    private final TronTransferServiceImpl tronTransferService;
    private final TronAccountServiceImpl tronAccountService;
    private final TronResourceExchangeServiceImpl tronResourceExchangeService;
    private final TronResourceRentalOrderServiceImpl tronResourceRentalOrderService;
    private final DepositOrderServiceImpl depositOrderService;
    private final RateLimiterService rateLimiterService;
    private final TronScanBlockServiceImpl scanBlockService;

    public Mono<TronResourceRentalOrder> createResourceRentalOrder(String id,String ip,TronResourceRentalRequest tronResourceRentalRequest){
        try {
            TronResourceRentalOrder order = new TronResourceRentalOrder(tronResourceExchangeService,tronResourceRentalRequest,ip,id);
            return Mono.just(order);
        } catch (ErrorType.ApplicationException e) {
            return Mono.error(e);
        }
    }
    public Mono<Tuple2<DepositOrder, TronAccount>> depositOrderBuilder(List<TronResourceRentalOrder> tronResourceRentalOrders){
        try {
            DepositOrder depositOrder = new DepositOrder(tronResourceRentalOrders);
            return depositOrderService.findUnexpiredOrders(depositOrder)
                    .map(DepositOrder::getReceivingAccountId)
                    .collectList()
                    .flatMap(tronAccountService::findReceivingAccount)
                    .switchIfEmpty(tronAccountService.findReceivingAccount(List.of()))
                    .flatMap(account -> depositOrderService.insert(depositOrder.updateReceivingAccount(account))
                            .map(d -> Tuples.of(d,account)))
                    .doOnNext(tuple2 -> {
                        List<String> ids = tronResourceRentalOrders.stream().map(TronResourceRentalOrder::getId).toList();
                        tronResourceRentalOrderService.updateDepositOrderId(ids,tuple2.getT1().getId()).subscribe();
                    });
        } catch (ErrorType.ApplicationException e) {
            return Mono.error(e);
        }
    }
    @GetMapping("/resource-message")
    public Mono<ResModel<ResourceMessage>> getAccountResourceMessage(){
        return Mono.just(ResModel.success(new ResourceMessage(tronResourceExchangeService.getAccountResourceMessage())));
    }

    @PostMapping("/rental")
    public Mono<ResModel<Tuple2<DepositOrder, TronAccount>>> createMultipleRentalOrder(@RequestBody List<TronResourceRentalRequest> tronResourceRentalRequests, ServerHttpRequest request){
        if (!scanBlockService.getEnableScanBlock()){
            return Mono.error(ErrorType.SYSTEM_UNDER_MAINTENANCE.getException());
        }
        String ip = IpUtils.getRealClientIpAddress(request);
        Mono<Tuple2<DepositOrder, TronAccount>> mono = Flux.fromIterable(tronResourceRentalRequests)
                .flatMap(TronResourceRentalRequest::verify)
                .all(Boolean::booleanValue)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(ErrorType.PARAMETER_IS_ILLEGAL.getException()))
                .flatMapMany(aBoolean -> Flux.fromIterable(tronResourceRentalRequests))
                .distinct(TronResourceRentalRequest::getReceivingAddress)
                .flatMap(tronResourceRentalRequest -> tronAccountService.findOrCreate(tronResourceRentalRequest.getReceivingAddress(), AccountType.EXTERNAL)
                        .flatMap(tronAccount -> createResourceRentalOrder(tronAccount.getId(),ip,tronResourceRentalRequest))
                        .flatMap(tronResourceRentalOrderService::create))
                .collectList()
                .flatMap(this::depositOrderBuilder)
                .doFinally(signalType -> {
                    rateLimiterService.clearLockedIp(ip).subscribe();
                });

        return rateLimiterService.isAllowedIp(ip)
                .then(mono)
                .map(ResModel::success);


    }
}
