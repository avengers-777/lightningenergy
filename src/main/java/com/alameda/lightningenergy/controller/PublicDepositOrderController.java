package com.alameda.lightningenergy.controller;

import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.data.DepositOrder;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronResourceRentalOrder;
import com.alameda.lightningenergy.service.DepositOrderServiceImpl;
import com.alameda.lightningenergy.service.TronAccountServiceImpl;
import com.alameda.lightningenergy.service.TronResourceRentalOrderServiceImpl;
import com.alameda.lightningenergy.utils.QueryTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.alameda.lightningenergy.entity.common.Common.DEVICE_ID_PROPERTIES;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/g/v1/tron/deposit")
public class PublicDepositOrderController {
    private final DepositOrderServiceImpl depositOrderService;
    private final TronResourceRentalOrderServiceImpl resourceRentalOrderService;
    private final TronAccountServiceImpl tronAccountService;


    @PostMapping("/order/search/{deviceId}")
    public Mono<ResModel<List<DepositOrder>>> search(@RequestBody QueryTool queryTool, @RequestParam("size") int size, @RequestParam("page") int page, @RequestParam("direction")Sort.Direction direction, @RequestParam("properties") String properties,@PathVariable("deviceId") String deviceId) {
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,DEVICE_ID_PROPERTIES,List.of(deviceId)));
        Query query = queryTool.build().with(PageRequest.of(page, size, Sort.by(direction,properties)));
        return depositOrderService.search(query,queryTool.build()).map(tuple -> ResModel.success(tuple.getT1(),tuple.getT2()));
    }
    @GetMapping("/order/{id}")
    public Mono<ResModel<DepositOrder>> findOne(@PathVariable("id") String id,@RequestParam("deviceId") String deviceId){
        return depositOrderService.findByDeviceId(deviceId,id).map(ResModel::success);
    }
    @GetMapping("/order/{id}/rental-order")
    public Mono<ResModel<List<TronResourceRentalOrder>>> findRentalOrder(@PathVariable("id") String id, @RequestParam("deviceId") String deviceId){
        Flux<TronResourceRentalOrder> resourceRentalOrders = depositOrderService.findByDeviceId(deviceId,id)
                .map(DepositOrder::getActions)
                .flatMapMany(resourceRentalOrderService::findByIds).share();
        Mono<Map<String, TronAccount>> tronAccountMap = resourceRentalOrders.map(TronResourceRentalOrder::getReceivingAccountId)
                .collectList()
                .flatMapMany(tronAccountService::findByIds)
                .collectMap(TronAccount::getId)
                .share();

        return tronAccountMap.flatMapMany(accountMap -> resourceRentalOrders.map(order -> {
                    String address  = accountMap.get(order.getReceivingAccountId()).getBase58CheckAddress();
                    order.setReceivingAccountId(address);
                    return order;
                }))
                .collectList()
                .map(ResModel::success);
    }
}
