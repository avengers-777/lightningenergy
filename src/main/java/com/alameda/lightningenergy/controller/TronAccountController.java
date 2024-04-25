package com.alameda.lightningenergy.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.data.Admin;
import com.alameda.lightningenergy.entity.data.Signature;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronActivationRecord;
import com.alameda.lightningenergy.entity.dto.AdminRequest;
import com.alameda.lightningenergy.service.AdminServiceImpl;
import com.alameda.lightningenergy.service.TronAccountServiceImpl;
import com.alameda.lightningenergy.utils.IpUtils;
import com.alameda.lightningenergy.utils.QueryTool;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
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
@RequestMapping("/a/v1/pri/tron/account")
public class TronAccountController {
    private final TronAccountServiceImpl tronAccountService;

    @PostMapping
    public Mono<ResModel<TronAccount>> generateAccount() {
        return tronAccountService.generateAccount().map(ResModel::success);
    }

    @PatchMapping("/{id}/status")
    public Mono<ResModel<UpdateResult>> updateStatus(@PathVariable("id") String id, @RequestParam("status")TronAccount.Status status) {
        return tronAccountService.updateStatus(id,status).map(ResModel::success);
    }
    @PatchMapping("/{id}/balance")
    public Mono<ResModel<UpdateResult>> updateBalance(@PathVariable("id") String id) {
        return tronAccountService.updateBalance(id).map(ResModel::success);
    }
    @PostMapping("/search")
    public Mono<ResModel<List<TronAccount>>> search(@RequestBody QueryTool queryTool,@RequestParam("size") int size, @RequestParam("page") int page, @RequestParam("direction")Sort.Direction direction, @RequestParam("properties") String properties) {
        Query query = queryTool.build().with(PageRequest.of(page, size, Sort.by(direction,properties)));
        return tronAccountService.search(query,queryTool.build()).map(tuple -> ResModel.success(tuple.getT1(),tuple.getT2()));
    }
}
