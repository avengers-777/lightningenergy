package com.alameda.lightningenergy.service;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alameda.lightningenergy.entity.data.Admin;
import com.alameda.lightningenergy.entity.data.Signature;
import com.alameda.lightningenergy.entity.dto.AdminRequest;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.mapper.AdminMapper;
import com.alameda.lightningenergy.utils.QueryTool;
import com.alameda.lightningenergy.utils.Utils;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.web3j.crypto.Keys;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminServiceImpl {
    private final AdminMapper adminMapper;

    public Mono<String> getNonce(String address) {
        return findByEthereumAddress(address)
                .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_DOES_NOT_EXIST.getException()))
                .map(Admin::getNonce);
    }

    public Mono<Admin> addAdmin(AdminRequest adminRequest) {
        return findByEthereumAddress(adminRequest.getEthereumAddress()).hasElement()
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_ALREADY_EXISTS.getException()))
                .then(adminMapper.insert(new Admin(adminRequest)));
    }

    public Mono<Admin> ethereumRegister(AdminRequest adminRequest) {
        QueryTool queryTool = new QueryTool();
        return adminMapper.count(queryTool.build()).filter(count -> count == 0)
                .switchIfEmpty(Mono.error(ErrorType.REGISTRATION_FAILED.getException()))
                .thenReturn(new Admin(adminRequest))
                .flatMap(adminMapper::insert);
    }

    public Mono<SaTokenInfo> login(Signature signature) {
        ServerWebExchange saTokenContext = SaReactorSyncHolder.getContext();
        return findByEthereumAddress(signature.getAddress())
                .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_DOES_NOT_EXIST.getException()))
                .flatMap(admin -> admin.verifyLoginSignature(signature))
                .flatMap(adminMapper::save)
                .map(admin -> {
                    SaReactorSyncHolder.setContext(saTokenContext);
                    StpUtil.login(admin.getId());
                    return StpUtil.getTokenInfo();
                });
    }

    public Mono<Admin> findByEthereumAddress(String address) {
        String checksumAddress = Keys.toChecksumAddress(address);
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, "ethereumAddress", List.of(checksumAddress)));
        return adminMapper.findOne(queryTool.build())
                ;
    }

    public Mono<Tuple2<List<Admin>, Long>> search(Query query, Query countQuery) {
        Mono<List<Admin>> orderListMono = adminMapper.find(query).collectList();
        Mono<Long> countQueryMono = adminMapper.count(countQuery);
        return Mono.zip(orderListMono, countQueryMono);
    }

    ;


    public Mono<Admin> update(AdminRequest request, String id) {

        Admin updateData = new Admin();
        BeanUtils.copyProperties(request, updateData);
        if (request.getEthereumAddress() != null) {
            updateData.setEthereumAddress(request.getEthereumAddress());
        }
        return adminMapper.findById(id)
                .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_DOES_NOT_EXIST.getException()))
                .filter(admin -> updateData.getEthereumAddress() == null || admin.getEthereumAddress().equals(updateData.getEthereumAddress()))
                .switchIfEmpty(Mono.error(ErrorType.CANNOT_MODIFY_ADDRESS.getException()))
                .flatMap(admin -> {
                    try {
                        Utils.merge(admin, updateData);
                        admin.setUpdateDate(System.currentTimeMillis());
                        return Mono.just(admin);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                }).flatMap(adminMapper::save);
    }

    ;

    public Mono<DeleteResult> delete(String id) {
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, "id", List.of(id)));
        return adminMapper.remove(queryTool.build());
    }
    public Mono<Admin> findById(String id){
        return adminMapper.findById(id);
    }
}
