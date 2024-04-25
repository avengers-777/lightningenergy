package com.alameda.lightningenergy.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.data.DepositOrder;
import com.alameda.lightningenergy.entity.data.TransferRecord;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.dto.TransferRequest;
import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.service.DepositOrderServiceImpl;
import com.alameda.lightningenergy.service.TronAccountServiceImpl;
import com.alameda.lightningenergy.service.TronTransferServiceImpl;
import com.alameda.lightningenergy.utils.IpUtils;
import com.alameda.lightningenergy.utils.QueryTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/a/v1/pri/tron/deposit")
public class DepositOrderController {
    private final DepositOrderServiceImpl depositOrderService;


    @PostMapping("/order/search")
    public Mono<ResModel<List<DepositOrder>>> search(@RequestBody QueryTool queryTool, @RequestParam("size") int size, @RequestParam("page") int page, @RequestParam("direction")Sort.Direction direction, @RequestParam("properties") String properties) {
        Query query = queryTool.build().with(PageRequest.of(page, size, Sort.by(direction,properties)));
        return depositOrderService.search(query,queryTool.build()).map(tuple -> ResModel.success(tuple.getT1(),tuple.getT2()));
    }
}
