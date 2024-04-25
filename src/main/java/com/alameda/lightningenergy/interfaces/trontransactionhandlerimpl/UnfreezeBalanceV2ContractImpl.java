package com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
import com.alameda.lightningenergy.entity.data.UnstakingResource;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.interfaces.TronTransactionHandler;
import com.alameda.lightningenergy.service.*;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import reactor.core.publisher.Mono;

import java.util.Map;

public class UnfreezeBalanceV2ContractImpl extends TronTransactionHandler {



    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.UnfreezeBalanceV2Contract result = Contract.UnfreezeBalanceV2Contract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention,transactionInfo,contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.getTransactionData().setUnfreezeBalanceV2Contract(result);
        return tronTransactionRecord;

    }

    @Override
    public Mono<TronTransactionRecord> eventHandler(
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
    ) {
        Contract.UnfreezeBalanceV2Contract unfreezeBalanceV2Contract = record.getTransactionData().getUnfreezeBalanceV2Contract();
        return Mono.justOrEmpty(unfreezeBalanceV2Contract)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()))
                .filter(data -> accountMap.containsKey(Base58Check.bytesToBase58(data.getOwnerAddress().toByteArray())))
                .flatMap(data -> {
                    String address = Base58Check.bytesToBase58(data.getOwnerAddress().toByteArray());
                    TronAccount tronAccount = accountMap.get(address);
                    UnstakingResource unstakingResource = new UnstakingResource(record.getTxid(),data.getResource(),data.getUnfreezeBalance(),record.getBlockHeight(),record.getBlockTimeStamp());
                    return tronAccountService.unFreezeBalance(tronAccount.getId(),unstakingResource).thenReturn(tronAccount);
                })
                .flatMap(account -> super.updateResource(record,tronAccountService,account,tronTransactionsService));





}}
