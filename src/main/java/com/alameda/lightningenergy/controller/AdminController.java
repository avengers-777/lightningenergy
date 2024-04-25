package com.alameda.lightningenergy.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.data.Admin;
import com.alameda.lightningenergy.entity.data.Signature;
import com.alameda.lightningenergy.entity.dto.AdminRequest;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.service.AdminServiceImpl;
import com.alameda.lightningenergy.utils.QueryTool;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/a/v1")
public class AdminController {
    private final AdminServiceImpl adminService;

    @GetMapping("/pub/admin/nonce/ethereum/{address}")
    public Mono<ResModel<String>> getEthereumAddressNonce(@PathVariable("address") String address) {
        return adminService.getNonce(address).map(ResModel::success);
    }
    @PostMapping("/pub/admin/register")
    public Mono<ResModel<Admin>> register(@RequestBody AdminRequest adminRequest) {
        return adminService.ethereumRegister(adminRequest).map(ResModel::success);
    }
    @PostMapping("/pub/admin/login")
    public Mono<ResModel<SaTokenInfo>> login(@RequestBody Signature signature) {
        return adminService.login(signature).map(ResModel::success);
    }
    @GetMapping("/pri/admin/profile")
    public Mono<ResModel<Admin>> getMyProfile(){
        String id = StpUtil.getLoginId().toString();
        return adminService.findById(id)
                .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_DOES_NOT_EXIST.getException()))
                .map(ResModel::success);
    }
    @PostMapping("/pri/admin/search")
    public Mono<ResModel<List<Admin>>> search(@RequestBody QueryTool queryTool,@RequestParam("size") int size, @RequestParam("page") int page, @RequestParam("direction")Sort.Direction direction, @RequestParam("properties") String properties) {
        Query query = queryTool.build().with(PageRequest.of(page, size, Sort.by(direction, properties)));
        return adminService.search(query,queryTool.build()).map(tuple -> ResModel.success(tuple.getT1(),tuple.getT2()));
    }
    @PostMapping("/pri/admin")
    public Mono<ResModel<Admin>> add(@RequestBody AdminRequest adminRequest) {
        return adminService.addAdmin(adminRequest).map(ResModel::success);
    }
    @PatchMapping("/pri/admin/{id}")
    public Mono<ResModel<Admin>> update(@RequestBody AdminRequest adminRequest,@PathVariable("id")String id) {
        return adminService.update(adminRequest,id).map(ResModel::success);
    }
    @DeleteMapping("/pri/admin/{id}")
    public Mono<ResModel<DeleteResult>> delete(@PathVariable("id")String id) {
        return adminService.delete(id).map(ResModel::success);
    }
    @DeleteMapping("/pri/admin/kickById/{id}")
    public Mono<ResModel<Object>> kickById(@PathVariable("id") String id) {
        StpUtil.kickout(id);
        return Mono.just(ResModel.success());
    }
    @DeleteMapping("/pri/admin/logout")
    public Mono<ResModel<Object>> logout(){
        StpUtil.logout();
        return Mono.just(ResModel.success());
    }

}
