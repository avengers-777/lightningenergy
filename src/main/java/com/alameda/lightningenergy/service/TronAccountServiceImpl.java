package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.*;
import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.entity.enums.TransactionType;
import com.alameda.lightningenergy.entity.enums.TransferStatus;
import com.alameda.lightningenergy.mapper.TronAccountMapper;
import com.alameda.lightningenergy.mapper.TronActivationRecordMapper;
import com.alameda.lightningenergy.utils.QueryTool;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.alameda.lightningenergy.entity.common.Common.*;
import static com.alameda.lightningenergy.entity.data.Balance.*;
import static com.alameda.lightningenergy.entity.data.TronAccount.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class TronAccountServiceImpl {
    private final Environment environment;
    private final TronAccountMapper tronAccountMapper;
    private final TronActivationRecordMapper tronActivationRecordMapper;
    private final RateLimiterService rateLimiterService;
    private final TronResourceExchangeServiceImpl tronResourceService;
    private final ApiWrapper apiWrapper;
    private final RASUtils rasUtils;
    public Flux<TronAccount> findAll(){
        return tronAccountMapper.findAll();
    }

    public Mono<UpdateResult> updateBalance(String accountId, long amount) {
        QueryTool queryTool = new QueryTool();

        Update update = new Update().inc(AMOUNT_PROPERTIES, amount).set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.findById(accountId).build(), update);

    }

    public Mono<UpdateResult> updateStatus(String accountId, TronAccount.Status status) {
        QueryTool queryTool = new QueryTool();
        Update update = new Update().set(STATUS_PROPERTIES, status).set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.findById(accountId).build(), update);
    }

    public Mono<UpdateResult> freezeBalance(String accountId, Common.ResourceCode resourceCode, long amount) {
        QueryTool queryTool = new QueryTool();
        Update update = new Update()
                .inc(AMOUNT_PROPERTIES, -amount)
                .inc(getResourceProperties(resourceCode), amount)
                .inc(getCanDelegatedResourceProperties(resourceCode), amount)
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.findById(accountId).build(), update);
    }

    public Mono<UpdateResult> unFreezeBalance(String accountId, UnstakingResource unstakingResource) {
        Common.ResourceCode resourceCode = unstakingResource.getResourceType();
        long amount = unstakingResource.getUnfreezeAmount();
        QueryTool queryTool = new QueryTool();
        Update update = new Update()
                .inc(getResourceProperties(resourceCode), -amount)
                .inc(getCanDelegatedResourceProperties(resourceCode), -amount)
                .addToSet(UNSTAKING_RESOURCES_PROPERTIES, unstakingResource)
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.findById(accountId).build(), update);
    }

    public Mono<UpdateResult> withdrawExpireUnfreeze(String accountId, UnstakingResource unstakingResource) {
        QueryTool queryTool = new QueryTool();
        queryTool.findById(accountId);
        Update update = new Update()
                .inc(AMOUNT_PROPERTIES, unstakingResource.getUnfreezeAmount())
                .pull(UNSTAKING_RESOURCES_PROPERTIES, unstakingResource)
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.build(), update);

    }

    public Mono<UpdateResult> cancelUnFreeze(String accountId, UnstakingResource unstakingResource) {
        Common.ResourceCode resourceCode = unstakingResource.getResourceType();
        long amount = unstakingResource.getUnfreezeAmount();
        QueryTool queryTool = new QueryTool();
        queryTool.findById(accountId);
        Update update = new Update()
                .inc(getResourceProperties(resourceCode), amount)
                .inc(getCanDelegatedResourceProperties(resourceCode), amount)
                .pull(UNSTAKING_RESOURCES_PROPERTIES, unstakingResource)
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.build(), update);
    }
    public Mono<UpdateResult> receiveResourcesUpdate(Common.ResourceCode resourceCode ,long amount,String accountId){
        QueryTool queryTool = new QueryTool();
        queryTool.findById(accountId);
        Update update = new Update()
                .inc(getAcquiredDelegatedResourceProperties(resourceCode), amount)
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.build(),update);
    }
    public Mono<UpdateResult> delegatedResourceUpdate(Common.ResourceCode resourceCode ,long amount,String accountId){
        QueryTool queryTool = new QueryTool();
        queryTool.findById(accountId);
        Update update = new Update()
                .inc(getCanDelegatedResourceProperties(resourceCode), Math.min(-amount,0L))
                .inc(getDelegatedResourceProperties(resourceCode),amount)
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.build(),update);
    }

    public Mono<UpdateResult> updateCanDelegatedResource(String tronAccountId,long canDelegatedForEnergy,long canDelegatedForBandwidth) {
        QueryTool queryTool = new QueryTool();

        Update update = new Update()
                .set(CAN_DELEGATED_FOR_ENERGY_PROPERTIES, canDelegatedForEnergy)
                .set(CAN_DELEGATED_FOR_BANDWIDTH_PROPERTIES, canDelegatedForBandwidth)
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return tronAccountMapper.updateFirst(queryTool.findById(tronAccountId).build(),update);
    }
    public Mono<UpdateResult> updateResource(TronTransactionRecord record) {
        QueryTool queryTool = new QueryTool();

        Update update = new Update().inc(AMOUNT_PROPERTIES, -record.getFee())
                .set(UPDATE_DATE_PROPERTIES, System.currentTimeMillis());
        return Mono.justOrEmpty(record)
                .map(TronTransactionRecord::getAccountId)
                .flatMap(id -> {

                    return tronAccountMapper.updateFirst(queryTool.findById(id).build(), update);
                });
    }

    public Mono<TronAccount> authorizedAccountBlockDataHandler(TronTransactionRecord record, Map<String, TronAccount> accountMap, Contract.AccountPermissionUpdateContract accountPermissionUpdateContract) {
        return findOrCreate(Base58Check.bytesToBase58(accountPermissionUpdateContract.getOwnerAddress().toByteArray()),AccountType.AUTHORIZED)
                .flatMap(tronAuthorizedAccount ->{
                        tronAuthorizedAccount.setPermissionSet(new HashSet<>());
                        return Flux.merge(Permission.ownerPermissionHandler(accountPermissionUpdateContract, record, accountMap), Permission.activesListHandler(accountPermissionUpdateContract, record, accountMap))
                                .reduce(tronAuthorizedAccount, (account, permission) -> {
                                    account.getPermissionSet().add(permission);
                                    return account;
                                });
                }
                )
                .filter(account -> !account.getPermissionSet().isEmpty())
                .flatMap(account ->
                        rateLimiterService.getBlockHeightAndUpdateTime()
                                .map(tuple2 -> {
                                    account.getBalance().updateResource(apiWrapper, account.getBase58CheckAddress(), tuple2.getT1(), tuple2.getT2());
                                    return account;
                                }));

    }


    public Mono<TronAccount> findByAddress(String address) {
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, ADDRESS_PROPERTIES, List.of(address)));
        return tronAccountMapper.findOne(queryTool.build());
    }


    public Mono<TronAccount> findOrCreate(String address, AccountType accountType) {

        Mono<TronAccount> accountMono = Mono.defer(() -> {
            TronAccount tronAuthorizedAccount = new TronAccount(address,accountType);
            return tronAccountMapper.insert(tronAuthorizedAccount);
        });
        Mono<TronAccount> mono = findByAddress(address)
                .switchIfEmpty(accountMono)
                .doFinally(signalType -> rateLimiterService.batchClearAddress(List.of(address)).subscribe());
        return rateLimiterService.batchLockAddress(List.of(address))
                .then(mono);

    }


    public Flux<TronAccount> findByAddressList(List<String> addressList) {
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, ADDRESS_PROPERTIES, addressList));
        return tronAccountMapper.find(queryTool.build());
    }

    public Mono<TronAccount> saveTronAccount(TronAccount tronAccount) {
        return tronAccountMapper.save(tronAccount);
    }

    public Mono<TronAccount> generateAccount() {
        try {
            TronAccount account = new TronAccount(KeyPair.generate(), rasUtils);
            return tronAccountMapper.insert(account);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }


    public void saveErrorRecord(TronActivationRecord record, String error) {
        record.setError(error);
        record.setUpdateDate(System.currentTimeMillis());
        record.setStatus(TransferStatus.ERROR);
        tronActivationRecordMapper.insert(record).subscribe();
    }

    public void saveSuccessRecord(TronActivationRecord record, String txid) {
        record.setTxid(txid);
        record.setUpdateDate(System.currentTimeMillis());
        record.setStatus(TransferStatus.SUCCESS);
        tronActivationRecordMapper.insert(record).subscribe();
    }




    public Mono<Tuple2<List<TronAccount>, Long>> search(Query query, Query countQuery) {
        Mono<List<TronAccount>> orderListMono = tronAccountMapper.find(query).collectList();
        Mono<Long> countQueryMono = tronAccountMapper.count(countQuery);
        return Mono.zip(orderListMono, countQueryMono);
    }

    ;

    public Mono<TronAccount> findById(String id) {

        return tronAccountMapper.findById(id)
                .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_DOES_NOT_EXIST.getException()));
    }
    public Flux<TronAccount> findByIds(List<String> ids) {
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,ID_PROPERTIES,ids));
        return tronAccountMapper.find(queryTool.build());
    }

    public Mono<UpdateResult> updateBalance(String id) {
        QueryTool queryTool = new QueryTool();
        return Mono.defer(() -> findById(id)
                .flatMap(account -> rateLimiterService.getBlockHeightAndUpdateTime()
                        .flatMap(tuple -> {
                            account.getBalance().updateResource(apiWrapper,account.getBase58CheckAddress(), tuple.getT1(), tuple.getT2());
                            Update update = new Update()
                                    .set(BALANCE_PROPERTIES,account.getBalance())
                                    .set(UPDATE_DATE_PROPERTIES,System.currentTimeMillis());
                            return tronAccountMapper.updateFirst(queryTool.findById(id).build(),update);
                        })));


    }

    public Mono<TronAccount> findReceivingAccount( List<String> excludeAddress)  {
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,STATUS_PROPERTIES,List.of(TronAccount.Status.ACTIVE,TronAccount.Status.INACTIVATED)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN, ACCOUNT_TYPE_PROPERTIES,List.of(AccountType.INTERNAL)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.NIN,ID_PROPERTIES,excludeAddress));
        return tronAccountMapper.findOne(queryTool.build())
                .switchIfEmpty(generateAccount());
    }


}
