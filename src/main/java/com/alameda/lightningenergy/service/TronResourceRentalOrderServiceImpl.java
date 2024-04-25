package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.Permission;
import com.alameda.lightningenergy.entity.data.TransferRecord;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronResourceRentalOrder;
import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.ContractType;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.mapper.TronAccountMapper;
import com.alameda.lightningenergy.mapper.TronResourceRentalOrderMapper;
import com.alameda.lightningenergy.utils.BlockUtils;
import com.alameda.lightningenergy.utils.QueryTool;
import com.google.protobuf.ByteString;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Common;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuples;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.alameda.lightningenergy.entity.common.Common.*;
import static com.alameda.lightningenergy.entity.data.Balance.getCanDelegatedResourceProperties;
import static com.alameda.lightningenergy.entity.data.Permission.OPERATIONS_PROPERTIES;
import static com.alameda.lightningenergy.entity.data.TronAccount.ACCOUNT_TYPE_PROPERTIES;
import static com.alameda.lightningenergy.entity.data.TronResourceRentalOrder.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class TronResourceRentalOrderServiceImpl {
    private final ApiWrapper apiWrapper;
    private final Environment environment;
    private final RASUtils rasUtils;
    private final TronResourceRentalOrderMapper tronResourceRentalOrderMapper;
    private final TronAccountMapper tronAccountMapper;
    private final RateLimiterService rateLimiterService;
    private final TronAccountServiceImpl tronAccountService;


    public Mono<TronResourceRentalOrder> create(TronResourceRentalOrder tronResourceRentalOrder) {
        return findCanDelegatedForResource(tronResourceRentalOrder.getResourceCode(), tronResourceRentalOrder.getValueInTrx())
                .switchIfEmpty(Mono.error(ErrorType.INSUFFICIENT_DELEGABLE_RESOURCES.getException()))
                .then(tronResourceRentalOrderMapper.insert(tronResourceRentalOrder));
    }

    public Mono<TronAccount> findCanDelegatedForResource(Common.ResourceCode resourceCode, long valueInTrx) {
        return rateLimiterService.getLockingTronAccount()
                .defaultIfEmpty(new HashSet<>())
                .flatMap(strings -> {
                    QueryTool queryTool = new QueryTool();
                    queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.NIN,ID_PROPERTIES,new ArrayList<>(strings)));
                    queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, STATUS_PROPERTIES, List.of(TronAccount.Status.ACTIVE)));
                    queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, ACCOUNT_TYPE_PROPERTIES, List.of(AccountType.AUTHORIZED)));
                    queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, OPERATIONS_PROPERTIES, List.of(ContractType.DelegateResourceContract)));
                    queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.GTE, getCanDelegatedResourceProperties(resourceCode), List.of(valueInTrx)));
                    return tronAccountMapper.findOne(queryTool.build());
                });

    }

    public Mono<TronResourceRentalOrder> findByTxid(String txid) {
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, TXID_PROPERTIES, List.of(txid)));
        return tronResourceRentalOrderMapper.findOne(queryTool.build());

    }

    public Flux<TronResourceRentalOrder> findByIds(List<String> ids) {
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, ID_PROPERTIES, ids));
        return tronResourceRentalOrderMapper.find(queryTool.build());
    }

    public Mono<UpdateResult> saveErrorStatus(String message, String id,String lessorId) {
        QueryTool queryTool = new QueryTool();
        Update update = new Update()
                .set(LESSOR_ID_PROPERTIES,lessorId)
                .set(ERROR_PROPERTIES, message)
                .set(STATUS_PROPERTIES, TronResourceRentalOrder.Status.ERROR);
        return tronResourceRentalOrderMapper.updateFirst(queryTool.findById(id).build(), update);
    }
    public Mono<UpdateResult> updateDepositOrderId(List<String> ids,String depositOrderId){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,ID_PROPERTIES,ids));
        Update update = new Update()
                .set(DEPOSIT_ORDER_ID_PROPERTIES,depositOrderId);
        return tronResourceRentalOrderMapper.updateMulti(queryTool.build(),update);
    }

    public Mono<UpdateResult> updateOrderSuccessStatus(String lessorId, String txid, String id, long duration) {
        return rateLimiterService.getBlockHeightAndUpdateTime()
                .flatMap(tuple2 -> {
                    QueryTool queryTool = new QueryTool();
                    long expectedReclaimTime = System.currentTimeMillis() + duration ;
                    long expectedReclaimBlockHeight = BlockUtils.getBlockHeightByTimestamp(tuple2.getT1(), tuple2.getT2(), expectedReclaimTime);
                    Update update = new Update()
                            .set(LESSOR_ID_PROPERTIES, lessorId)
                            .set(EXPECTED_RECLAIM_TIME_PROPERTIES, expectedReclaimTime)
                            .set(EXPECTED_RECLAIM_BLOCK_HEIGHT_PROPERTIES, expectedReclaimBlockHeight)
                            .set(TXID_PROPERTIES, txid)
                            .set(TRANSACTION_TIME_PROPERTIES, System.currentTimeMillis())
                            .set(STATUS_PROPERTIES, Status.PENDING_RECLAIM);
                    return tronResourceRentalOrderMapper.updateFirst(queryTool.findById(id).build(), update);
                });
    }
    public Mono<UpdateResult> markOrderAsCompletedWithTxid(String orderId,String txid){
        QueryTool queryTool = new QueryTool();
        Update update = new Update()
                .set(RECLAIM_TXID_PROPERTIES, txid)
                .set(COMPLETE_TIME_PROPERTIES, System.currentTimeMillis())
                .set(STATUS_PROPERTIES, Status.COMPLETED);
        return tronResourceRentalOrderMapper.updateFirst(queryTool.findById(orderId).build(), update);
    }
    public Mono<UpdateResult> markOrderAsReclaimFailed(String orderId, String errorMessage) {
        QueryTool queryTool = new QueryTool();
        Update update = new Update()
                .set(ERROR_PROPERTIES, errorMessage)
                .set(STATUS_PROPERTIES, Status.RECLAIM_FAILED);
        return tronResourceRentalOrderMapper.updateFirst(queryTool.findById(orderId).build(), update);
    }
    public Mono<Tuple5<TronAccount,TronAccount,TronAccount,TronResourceRentalOrder,Permission>> retrieveRentalOrderDetails(TronResourceRentalOrder order){
        Mono<TronAccount> receivingAccount = tronAccountService.findById(order.getReceivingAccountId());
        Mono<TronAccount> lessorAccount = tronAccountService.findById(order.getLessorId());
        return Mono.zip(receivingAccount,lessorAccount)
                .flatMap(tuple2 -> tuple2.getT2().pickOnePermission()
                        .flatMap(permission -> tronAccountService.findById(permission.getAuthorizedTo())
                                .map(authorizedToAccount -> Tuples.of(tuple2.getT1(),tuple2.getT2(),authorizedToAccount,order,permission))
                        ));
    }
    public Mono<UpdateResult> executeResourceReclamationTransaction(Tuple5<TronAccount,TronAccount,TronAccount,TronResourceRentalOrder,Permission> tuple5){

        try {
            String orderId = tuple5.getT4().getId();
            TronAccount authorizedToAccount = tuple5.getT3();
            authorizedToAccount.init(rasUtils,apiWrapper);

            Mono<UpdateResult> mono = authorizedToAccount.unDelegateResourceFromAuthorizedAccount(tuple5.getT2().getBase58CheckAddress(),tuple5.getT4().getValueInTrx(),tuple5.getT4().getResourceCode(), tuple5.getT1().getBase58CheckAddress(),tuple5.getT5().getPermissionId())
                    .map(tuple2 -> authorizedToAccount.getApiWrapper().broadcastTransaction(tuple2.getT2()))
                    .flatMap(txid -> markOrderAsCompletedWithTxid(orderId,txid))
                    .onErrorResume(e->markOrderAsReclaimFailed(orderId,e.getMessage()))
                    .doFinally(signalType -> rateLimiterService.clearLockedRentalOrder(orderId).subscribe());

            return rateLimiterService.lockingRentalOrder(orderId)
                    .then(mono);
        } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                 NoSuchAlgorithmException | UnsupportedEncodingException | IllegalBlockSizeException e) {
            return Mono.error(e);
        }
    }
    public Flux<UpdateResult> processResourceReclamation(){
        return findOrdersEligibleForReclamation()
                .delayElements(Duration.ofSeconds(3))
                .flatMap(this::retrieveRentalOrderDetails)
                .flatMap(this::executeResourceReclamationTransaction);

    }
    public Flux<TronResourceRentalOrder> findOrdersEligibleForReclamation(){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.LT,EXPECTED_RECLAIM_TIME_PROPERTIES,List.of(System.currentTimeMillis())));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,STATUS_PROPERTIES,List.of(Status.PENDING_RECLAIM)));
        return tronResourceRentalOrderMapper.find(queryTool.build());
    }

    public Mono<String> executeResourceDelegation(Tuple2<TronAccount, TronAccount> accountTronAccountTuple2, TronResourceRentalOrder rentalOrder, TronAccount lessorAccount, Permission permission) {
        try {
            accountTronAccountTuple2.getT1().init(rasUtils, apiWrapper);
            return accountTronAccountTuple2.getT1().delegateResourceFromAuthorizedAccount(lessorAccount, accountTronAccountTuple2.getT2().getBase58CheckAddress(), rentalOrder, permission)
                    .map(tuple2 -> accountTronAccountTuple2.getT1().getApiWrapper().broadcastTransaction(tuple2.getT2()))
                    .flatMap(txid -> updateOrderSuccessStatus(lessorAccount.getId(), txid, rentalOrder.getId(), rentalOrder.getDuration())
                            .flatMap(r -> tronAccountService.delegatedResourceUpdate(rentalOrder.getResourceCode(),rentalOrder.getValueInTrx(),lessorAccount.getId()).thenReturn(txid)));
        } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                 NoSuchAlgorithmException | UnsupportedEncodingException |
                 IllegalBlockSizeException e) {
            return Mono.error(e);
        }
    }

    public Mono<String> processResourceRentalDelegation(TronResourceRentalOrder rentalOrder, TronAccount lessorAccount) {
        Mono<String> mono = lessorAccount.pickOnePermission().flatMap(permission -> {
                            Mono<TronAccount> authorizedToAccountMono = tronAccountMapper.findById(permission.getAuthorizedTo());
                            Mono<TronAccount> receivingAccountMono = tronAccountMapper.findById(rentalOrder.getReceivingAccountId());
                            return Mono.zip(authorizedToAccountMono, receivingAccountMono)
                                    .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_DOES_NOT_EXIST.getException()))
                                    .flatMap(accountTronAccountTuple2 -> executeResourceDelegation(accountTronAccountTuple2, rentalOrder, lessorAccount, permission));
                        }
                )
                .onErrorResume(e->saveErrorStatus(e.getMessage(), rentalOrder.getId(),lessorAccount.getId()).then(Mono.empty()))
                .doFinally(signalType -> {
                    rateLimiterService.clearLockedRentalOrder(rentalOrder.getId()).subscribe();
                    rateLimiterService.unlockTronAccount(lessorAccount.getId()).subscribe();
                });

        return rateLimiterService.lockingRentalOrder(rentalOrder.getId())
                .then(rateLimiterService.lockingTronAccount(lessorAccount.getId()))
                .then(mono);

    }
    public Flux<TronResourceRentalOrder> findRentalOrdersInLast24Hours(String lessorId){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,LESSOR_ID_PROPERTIES,List.of(lessorId)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,STATUS_PROPERTIES,List.of(Status.COMPLETED)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.GTE,COMPLETE_TIME_PROPERTIES,List.of(System.currentTimeMillis() - Duration.ofDays(1).toMillis())));
        return tronResourceRentalOrderMapper.find(queryTool.build());
    };

    public Mono<Tuple2<List<TronResourceRentalOrder>, Long>> search(Query query, Query countQuery) {
        Mono<List<TronResourceRentalOrder>> listMonoistMono = tronResourceRentalOrderMapper.find(query).collectList();
        Mono<Long> countQueryMono = tronResourceRentalOrderMapper.count(countQuery);
        return Mono.zip(listMonoistMono, countQueryMono);
    }
}
