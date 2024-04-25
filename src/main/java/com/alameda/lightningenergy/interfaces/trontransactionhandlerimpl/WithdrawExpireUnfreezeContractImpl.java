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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class WithdrawExpireUnfreezeContractImpl extends TronTransactionHandler {



    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.WithdrawExpireUnfreezeContract result = Contract.WithdrawExpireUnfreezeContract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention,transactionInfo,contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.getTransactionData().setWithdrawExpireUnfreezeContract(result);
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
        Contract.WithdrawExpireUnfreezeContract withdrawExpireUnfreezeContract = record.getTransactionData().getWithdrawExpireUnfreezeContract();
        return Mono.justOrEmpty(withdrawExpireUnfreezeContract)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()))
                .filter(data -> accountMap.containsKey(Base58Check.bytesToBase58(data.getOwnerAddress().toByteArray())))
                .flatMap(data -> {
                    String address = Base58Check.bytesToBase58(data.getOwnerAddress().toByteArray());
                    TronAccount tronAccount = accountMap.get(address);
                    return Flux.fromIterable(tronAccount.getBalance().getUnstakingResources())
                            .filter(unstakingResource -> unstakingResource.isUnlocked(record.getBlockHeight()))
                            .flatMap(unstakingResource -> tronAccountService.withdrawExpireUnfreeze(tronAccount.getId(),unstakingResource))
                            .collectList()
                            .thenReturn(record)
                            .flatMap(r -> super.updateResource(r,tronAccountService,tronAccount,tronTransactionsService));
                });

    }




}
