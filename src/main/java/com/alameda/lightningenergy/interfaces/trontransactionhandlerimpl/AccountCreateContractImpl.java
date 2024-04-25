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

public class AccountCreateContractImpl extends TronTransactionHandler {



    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.AccountCreateContract result = Contract.AccountCreateContract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention,transactionInfo,contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.getTransactionData().setAccountCreateContract(result);
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
        Contract.AccountCreateContract data = record.getTransactionData().getAccountCreateContract();
        return Mono.justOrEmpty(data)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()))
                .filter(accountCreateContract -> accountMap.containsKey(Base58Check.bytesToBase58(accountCreateContract.getAccountAddress().toByteArray())))
                .flatMap(accountCreateContract -> {
                    TronAccount tronAccount = accountMap.get(Base58Check.bytesToBase58(accountCreateContract.getAccountAddress().toByteArray()));
                    if (tronAccount.getStatus().equals(TronAccount.Status.INACTIVATED)){
                        return tronAccountService.updateStatus(tronAccount.getId(), TronAccount.Status.ACTIVE).thenReturn(tronAccount);
                    }
                    return Mono.just(tronAccount);
                })
                .flatMap(tronAccount -> super.updateResource(record,tronAccountService,tronAccount,tronTransactionsService));
    }



}
