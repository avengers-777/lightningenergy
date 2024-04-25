package com.alameda.lightningenergy.interfaces;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
import com.alameda.lightningenergy.entity.enums.ContractType;
import com.alameda.lightningenergy.service.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import reactor.core.publisher.Mono;

import java.util.Map;


@Getter
public abstract class TronTransactionHandler {


    public abstract TronTransactionRecord handler(Response.TransactionExtention transactionExtention,Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException;

    public Chain.Transaction.Contract getContract(Response.TransactionExtention transactionExtention){
        Chain.Transaction.Contract contract = transactionExtention.getTransaction().getRawData().getContract(0);
        return contract;
    }
    public ContractType getContractType(Chain.Transaction.Contract contract){
        return ContractType.getContractTypeByNum(contract.getType().getNumber());
    }
    public TronTransactionRecord getTransactionRecord(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo,Chain.Transaction.Contract contract){

        TronTransactionRecord record = new TronTransactionRecord(transactionInfo,transactionExtention,getContractType(contract));
        return record;
    }
    public ByteString getRawDataValue(Chain.Transaction.Contract contract){
        return contract.getParameter().getValue();
    }
    public Mono<TronTransactionRecord> saveSuccessRecord(TronTransactionRecord record,TronTransactionsServiceImpl tronTransactionsService){
        record.setStatus(TronTransactionRecord.Status.COMPLETED);
        return tronTransactionsService.saveTronTransactionRecord(record);
    }
    public Mono<TronTransactionRecord> saveErrorRecord(TronTransactionRecord record,TronTransactionsServiceImpl tronTransactionsService,String e){
        record.setStatus(TronTransactionRecord.Status.ERROR);
        record.setError(e);
        return tronTransactionsService.saveTronTransactionRecord(record);
    }
    public Mono<TronAccount> findAccount(TronTransactionRecord record,Map<String, TronAccount> accountMap){
        return  Mono.justOrEmpty(accountMap.get(record.getAddress()));
    }
    public Mono<TronTransactionRecord> updateResource(TronTransactionRecord record,TronAccountServiceImpl tronAccountService,Map<String, TronAccount> accountMap,TronTransactionsServiceImpl tronTransactionsService){
        return  Mono.justOrEmpty(accountMap.get(record.getAddress()))
                .map(record::update)
                .flatMap(r -> tronAccountService.updateResource(r).thenReturn(r))
                .doOnNext(r -> saveSuccessRecord(r,tronTransactionsService).subscribe())
                .doOnError(e -> saveErrorRecord(record,tronTransactionsService,e.getMessage()).subscribe());
    }
    public Mono<TronTransactionRecord> updateResource(TronTransactionRecord record,TronAccountServiceImpl tronAccountService, TronAccount tronAccount,TronTransactionsServiceImpl tronTransactionsService){
        return  Mono.justOrEmpty(tronAccount)
                .map(record::update)
                .flatMap(r -> tronAccountService.updateResource(r).thenReturn(r))
                .doOnNext(r -> saveSuccessRecord(r,tronTransactionsService).subscribe())
                .doOnError(e -> saveErrorRecord(record,tronTransactionsService,e.getMessage()).subscribe());
    }






    public abstract Mono<TronTransactionRecord> eventHandler(
            TronTransferServiceImpl tronTransferService,
            TronResourceRentalOrderServiceImpl tronResourceRentalOrderService,
            TronTransactionsServiceImpl tronTransactionsService,
            DepositOrderServiceImpl depositOrderService,
            RateLimiterService rateLimiterService,
            TronResourceExchangeServiceImpl tronResourceService,
            TronAccountServiceImpl tronAccountService,
            RASUtils rasUtils,
            ApiWrapper apiWrapper,
            TronTransactionRecord record,
            Map<String, TronAccount> accountMap
    );
}
