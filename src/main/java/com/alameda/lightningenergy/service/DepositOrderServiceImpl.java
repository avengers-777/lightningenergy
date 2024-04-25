package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.entity.data.DepositOrder;
import com.alameda.lightningenergy.entity.enums.BlockchainPlatform;
import com.alameda.lightningenergy.entity.enums.Currency;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.mapper.TronDepositOrderMapper;
import com.alameda.lightningenergy.utils.QueryTool;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

import static com.alameda.lightningenergy.entity.common.Common.*;
import static com.alameda.lightningenergy.entity.data.DepositOrder.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepositOrderServiceImpl {
    private final ApiWrapper apiWrapper;
    private final Environment environment;
    private final TronDepositOrderMapper tronDepositOrderMapper;


    public Flux<DepositOrder> findUnexpiredOrders(DepositOrder depositOrder){
        QueryTool queryTool = buildBlockchainDepositQueryTool(List.of(depositOrder.getAmount(),depositOrder.getAmount() - depositOrder.getCurrency().calculateValue(1)),depositOrder.getCurrency(),depositOrder.getPlatform(),System.currentTimeMillis());
        return tronDepositOrderMapper.find(queryTool.build());
    }

    public Mono<DepositOrder> insert(DepositOrder depositOrder){
        return tronDepositOrderMapper.insert(depositOrder);
    }

    public Mono<DepositOrder> findByOrder(long amount, Currency currency, BlockchainPlatform platform, String accountId, Long blockTimeStamp){
        QueryTool queryTool = buildBlockchainDepositQueryTool(List.of(amount),currency,platform,blockTimeStamp);
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,RECEIVING_ACCOUNT_ID_PROPERTIES,List.of(accountId)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.LT,CREATE_DATE_PROPERTIES,List.of(blockTimeStamp)));
        Query query = queryTool.build().with(Sort.by(Sort.Direction.DESC,ID_PROPERTIES));
        return tronDepositOrderMapper.findOne(query);

    }
    public QueryTool buildBlockchainDepositQueryTool(List<Long> amount, Currency currency, BlockchainPlatform platform,Long timeStamp){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,STATUS_PROPERTIES, List.of(DepositOrder.DepositStatus.PENDING)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.GT,EXPIRATION_DATE_PROPERTIES,List.of(timeStamp)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,CURRENCY_PROPERTIES,List.of(currency)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,AMOUNT_PROPERTIES,amount));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,PLATFORM_PROPERTIES,List.of(platform)));

        return queryTool;
    }

    public Mono<UpdateResult> updateDepositOrder(String orderId,String fromAddress,String txid,long blockHeight,long receivingDate){
        Update update = new Update()
                .set(FROM_ADDRESS_PROPERTIES,fromAddress)
                .set(TXID_PROPERTIES,txid)
                .set(BLOCK_HEIGHT_PROPERTIES,blockHeight)
                .set(RECEIVING_DATE_PROPERTIES,receivingDate)
                .set(STATUS_PROPERTIES,DepositStatus.RECEIVED);
        QueryTool queryTool = new QueryTool();
        return tronDepositOrderMapper.updateFirst(queryTool.findById(orderId).build(),update);
    }


    public Mono<Tuple2<List<DepositOrder>, Long>> search(Query query, Query countQuery) {
        Mono<List<DepositOrder>> listMonoistMono = tronDepositOrderMapper.find(query).collectList();
        Mono<Long> countQueryMono = tronDepositOrderMapper.count(countQuery);
        return Mono.zip(listMonoistMono, countQueryMono);
    }
    public Mono<DepositOrder> findByDeviceId(String deviceId ,String id){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,ID_PROPERTIES,List.of(id)));
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,DEVICE_ID_PROPERTIES,List.of(deviceId)));
        return tronDepositOrderMapper.findOne(queryTool.build())
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()));
    }
}
