package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
import com.alameda.lightningenergy.entity.enums.ContractType;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.mapper.TronTransactionsMapper;
import com.alameda.lightningenergy.utils.QueryTool;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.alameda.lightningenergy.entity.data.TronTransactionRecord.ACCOUNT_ID_PROPERTIES;
import static com.alameda.lightningenergy.entity.data.TronTransactionRecord.BLOCK_TIME_STAMP_PROPERTIES;


@Service
@Slf4j
@RequiredArgsConstructor
public class TronTransactionsServiceImpl {
    private final TronTransactionsMapper tronTransactionsMapper;
    private final RateLimiterService rateLimiterService;
    private final TronAccountServiceImpl tronAccountService;
    private final RASUtils rasUtils;
    private final ApiWrapper apiWrapper;
    private final TronResourceExchangeServiceImpl tronResourceService;
    private final TronTransferServiceImpl tronTransferService;
    private final TronResourceRentalOrderServiceImpl tronResourceRentalOrderService;
    private final DepositOrderServiceImpl depositOrderService;


    public Flux<TronTransactionRecord> blockEventMainHandler(Response.TransactionInfoList transactionInfoList, Flux<Response.TransactionExtention> transactionExtentionFlux) {
        Flux<Tuple2<Response.TransactionExtention, Response.TransactionInfo>> filteredTransactionPairFlux = getFilteredTransactionPair(transactionInfoList, transactionExtentionFlux).share();
        Flux<TronTransactionRecord> convertToTransactionRecordFlux = convertToTransactionRecord(filteredTransactionPairFlux).share();
        Mono<List<String>> allAddress = getAllRelevantAddresses(convertToTransactionRecordFlux).distinct().collectList().share();
        Mono<Map<String, TronAccount>> allTronAccount = allAddress.flatMapMany(tronAccountService::findByAddressList).collectMap(TronAccount::getBase58CheckAddress).defaultIfEmpty(new HashMap<>());

        return transactionsHandler(convertToTransactionRecordFlux, allTronAccount);
    }

    public Flux<TronTransactionRecord> transactionsHandler(Flux<TronTransactionRecord> convertToTransactionRecordFlux, Mono<Map<String, TronAccount>> allTronAccount) {

        return allTronAccount.flatMapMany(accountMap -> convertToTransactionRecordFlux
                .flatMap(record -> record.getContractType().getHandler()
                        .eventHandler(tronTransferService,tronResourceRentalOrderService,this,depositOrderService,rateLimiterService,tronResourceService,tronAccountService,rasUtils,apiWrapper, record, accountMap)
                        .onErrorResume(e -> Mono.empty())
                )
        );
    }
    public Mono<TronTransactionRecord> saveTronTransactionRecord(TronTransactionRecord tronTransactionRecord){
        return findByTxid(tronTransactionRecord.getTxid())
                .hasElement()
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_ALREADY_EXISTS.getException()))
                .thenReturn(tronTransactionRecord)
                .flatMap(tronTransactionsMapper::insert);


    }

    public Mono<TronTransactionRecord> findByTxid(String txid){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,"txid",List.of(txid)));
        return tronTransactionsMapper.findOne(queryTool.build());
    }

    public Flux<String> getAllRelevantAddresses(Flux<TronTransactionRecord> convertToTransactionRecordFlux) {

        return convertToTransactionRecordFlux
                .filter(Objects::nonNull)
                .flatMap(record -> {
                            Flux<String> flux1 = record.getTransactionData()
                                    .getAllAddress();
                            return Flux.merge(flux1, Mono.justOrEmpty(record.getAddress()),Mono.justOrEmpty(record.getToAddress()));
                        }
                );
    }

    public Flux<TronTransactionRecord> convertToTransactionRecord(Flux<Tuple2<Response.TransactionExtention, Response.TransactionInfo>> filteredTransactionPairFlux) {
        return filteredTransactionPairFlux
                .flatMap(pair -> {
                    try {
                        TronTransactionRecord record = ContractType.getContractTypeByNum(pair.getT1().getTransaction().getRawData().getContract(0).getType().getNumber()).getHandler().handler(pair.getT1(), pair.getT2());
                        return Mono.just(record);
                    } catch (InvalidProtocolBufferException e) {
                        return Mono.error(e);
                    }
                });
    }


    public Flux<Tuple2<Response.TransactionExtention, Response.TransactionInfo>> getFilteredTransactionPair(Response.TransactionInfoList transactionInfoList, Flux<Response.TransactionExtention> transactionExtentionFlux) {
        return transactionExtentionFlux.reduce(new HashMap<String, Response.TransactionExtention>(), (map, transactionExtention) -> {
            map.put(Hex.toHexString(transactionExtention.getTxid().toByteArray()), transactionExtention);
            return map;
        }).flatMapMany(map -> Flux.fromIterable(transactionInfoList.getTransactionInfoList())
                .filter(transactionInfo -> map.containsKey(Hex.toHexString(transactionInfo.getId().toByteArray())))
                .map(transactionInfo -> Tuples.of(map.get(Hex.toHexString(transactionInfo.getId().toByteArray())), transactionInfo))
        );
    }

    public Flux<TronTransactionRecord> findTransactionsInTimeRange(String accountId, List<Long> timeRange){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,ACCOUNT_ID_PROPERTIES,List.of(accountId)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.GT_AND_LT,BLOCK_TIME_STAMP_PROPERTIES,timeRange));
        return tronTransactionsMapper.find(queryTool.build());
    };


}
