package com.alameda.lightningenergy.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.data.TransferRecord;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronActivationRecord;
import com.alameda.lightningenergy.entity.dto.TransferRequest;
import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.Currency;
import com.alameda.lightningenergy.entity.enums.ErrorType;
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
import reactor.util.function.Tuple2;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/a/v1/pri/tron/transfer")
public class TronTransferController {
    private final TronTransferServiceImpl tronTransferService;
    private final TronAccountServiceImpl tronAccountService;

    @PostMapping()
    public Mono<ResModel<TransferRecord>> transfer(@RequestBody TransferRequest transferRequest, ServerHttpRequest request) {
        String userId = StpUtil.getLoginId().toString();
        String ip = IpUtils.getRealClientIpAddress(request);
        return Mono.defer(()->{
            switch (transferRequest.getType()){
                case EXTERNAL -> {
                    return tronAccountService.findById(transferRequest.getFrom())
                            .flatMap(account -> tronTransferService.externalTransfer(account,transferRequest.getTo(),transferRequest.getAmount(),userId,ip));
                }
                default -> {
                    Mono<TronAccount> from = tronAccountService.findById(transferRequest.getFrom());
                    Mono<TronAccount> to = tronAccountService.findById(transferRequest.getTo())
                            .filter(tronAccount -> tronAccount.getAccountType().equals(AccountType.INTERNAL));
                    return Mono.zip(from,to)
                            .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_DOES_NOT_EXIST.getException()))
                            .flatMap(tuple2 -> tronTransferService.internalTransfer(tuple2.getT1(),tuple2.getT2(),transferRequest.getAmount(),userId,ip));
                }
            }
        }).map(ResModel::success);


    }
    @PostMapping("/search")
    public Mono<ResModel<List<TransferRecord>>> search(@RequestBody QueryTool queryTool,@RequestParam("size") int size, @RequestParam("page") int page, @RequestParam("direction")Sort.Direction direction, @RequestParam("properties") String properties) {
        Query query = queryTool.build().with(PageRequest.of(page, size, Sort.by(direction,properties)));
        return tronTransferService.search(query,queryTool.build()).map(tuple -> ResModel.success(tuple.getT1(),tuple.getT2()));
    }
}
