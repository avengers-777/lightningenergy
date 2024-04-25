package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.enums.TransferStatus;
import com.alameda.lightningenergy.entity.enums.TransactionType;
import com.alameda.lightningenergy.entity.data.TransferRecord;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.enums.Currency;
import com.alameda.lightningenergy.mapper.TransferRecordMapper;

import com.alameda.lightningenergy.utils.QueryTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class TronTransferServiceImpl {
    private final Environment environment;
    private final TransferRecordMapper recordMapper;
    private final RateLimiterService rateLimiterService;

    private final RASUtils rasUtils;
    private final ApiWrapper apiWrapper;


    public Mono<TransferRecord> transfer(TronAccount account, String toAddress, long amount,TransferRecord record) {
        Mono<TransferRecord> mono = Mono.defer(() -> {
                    try {
                        account.init(rasUtils,apiWrapper);
                        Tuple2<Response.TransactionExtention, Chain.Transaction> tuple2 = account.transfer(toAddress, amount);
                        return Mono.just(tuple2);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .map(tuple2 -> {
                    String txid = Hex.toHexString(tuple2.getT1().getTxid().toByteArray());
                    record.setTxid(txid) ;
                    return Tuples.of(tuple2.getT1(), tuple2.getT2(), txid);
                })
                .doOnError(e -> saveError(e, record))
                .doOnNext(tuple -> {
                    try{
                        account.getApiWrapper().broadcastTransaction(tuple.getT2());
                        saveSuccess(record);
                    }catch (Exception e){
                        saveError(e, record);
                    }
                })
                .thenReturn(record)
                .doFinally(signalType -> {
                    rateLimiterService.unlockTronAccount(account.getId()).subscribe();
                });
        return rateLimiterService.lockingTronAccount(account.getId())
                .then(mono);

    }
    public Mono<TransferRecord> findByTxid(String txid){
        QueryTool queryTool = new QueryTool();
        queryTool.addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,"txid", List.of(txid)));
        return recordMapper.findOne(queryTool.build());
    }

    public Mono<TransferRecord> internalTransfer(TronAccount from, TronAccount to, long amount, String userId, String ip) {
        TransferRecord transferRecord = new TransferRecord(TransactionType.INTERNAL, userId, ip, Currency.TRX, from.getId(), to.getId(), amount);


        return transfer(from, to.getBase58CheckAddress(), amount,transferRecord);
    }

    public Mono<TransferRecord> externalTransfer(TronAccount from, String toAddress, long amount, String userId, String ip) {
        TransferRecord transferRecord = new TransferRecord(TransactionType.EXTERNAL, userId, ip, Currency.TRX, from.getId(), toAddress, amount);

        return transfer(from, toAddress, amount,transferRecord);
    }

    public void saveSuccess( TransferRecord transferRecord) {
        transferRecord.setStatus(TransferStatus.SUCCESS);
        transferRecord.setUpdateDate(System.currentTimeMillis());
        recordMapper.insert(transferRecord).subscribe();
    }

    public void saveError(Throwable e, TransferRecord transferRecord) {
        transferRecord.setError(e.getMessage());
        transferRecord.setStatus(TransferStatus.ERROR);
        transferRecord.setUpdateDate(System.currentTimeMillis());
        recordMapper.insert(transferRecord).subscribe();
    }

    public Mono<Tuple2<List<TransferRecord>, Long>> search(Query query, Query countQuery) {
        Mono<List<TransferRecord>> listMonoistMono = recordMapper.find(query).collectList();
        Mono<Long> countQueryMono = recordMapper.count(countQuery);
        return Mono.zip(listMonoistMono, countQueryMono);
    }


}
