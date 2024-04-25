package com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
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

public class FreezeBalanceV2ContractImpl extends TronTransactionHandler {



    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.FreezeBalanceV2Contract result = Contract.FreezeBalanceV2Contract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention,transactionInfo,contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.getTransactionData().setFreezeBalanceV2Contract(result);
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
        Contract.FreezeBalanceV2Contract data = record.getTransactionData().getFreezeBalanceV2Contract();
        return Mono.justOrEmpty(data)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()))
                .filter(freezeBalanceV2Contract -> accountMap.containsKey(Base58Check.bytesToBase58(freezeBalanceV2Contract.getOwnerAddress().toByteArray())))
                .flatMap(freezeBalanceV2Contract -> {
                    String address = Base58Check.bytesToBase58(freezeBalanceV2Contract.getOwnerAddress().toByteArray());
                    TronAccount tronAccount = accountMap.get(address);
                    return tronAccountService.freezeBalance(tronAccount.getId(),freezeBalanceV2Contract.getResource(),freezeBalanceV2Contract.getFrozenBalance()).thenReturn(tronAccount);

                })
                .flatMap(account -> super.updateResource(record,tronAccountService,account,tronTransactionsService));
    }



}
