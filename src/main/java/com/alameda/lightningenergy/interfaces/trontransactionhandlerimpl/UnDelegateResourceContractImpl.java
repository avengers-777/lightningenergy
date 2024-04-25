package com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.interfaces.TronTransactionHandler;
import com.alameda.lightningenergy.service.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.client.result.UpdateResult;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import reactor.core.publisher.Mono;

import java.util.Map;

public class UnDelegateResourceContractImpl extends TronTransactionHandler {



    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.UnDelegateResourceContract result = Contract.UnDelegateResourceContract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention,transactionInfo,contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.getTransactionData().setUnDelegateResourceContract(result);
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
        Contract.UnDelegateResourceContract unDelegateResourceContract = record.getTransactionData().getUnDelegateResourceContract();
        return Mono.justOrEmpty(unDelegateResourceContract)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()))
                .filter(d -> filter(d,accountMap))
                .flatMap( data -> {
                    String receiverAddress = Base58Check.bytesToBase58(data.getReceiverAddress().toByteArray());
                    String ownerAddress = Base58Check.bytesToBase58(data.getOwnerAddress().toByteArray());
                    TronAccount receiverAccount = accountMap.get(receiverAddress);
                    TronAccount ownerAccount = accountMap.get(ownerAddress);
                    Mono<UpdateResult> receiverAccountUpdateMono = Mono.justOrEmpty(receiverAccount)
                            .flatMap(tronAccount -> tronAccountService.receiveResourcesUpdate(data.getResource(),-data.getBalance(),tronAccount.getId()));

                    Mono<UpdateResult> ownerAccountUpdateMono = Mono.justOrEmpty(ownerAccount)
                            .flatMap(tronAccount -> tronAccountService.delegatedResourceUpdate(data.getResource(),-data.getBalance(),tronAccount.getId()));

                    return Mono.when(receiverAccountUpdateMono, ownerAccountUpdateMono)
                            .thenReturn(record)
                            .flatMap(r -> super.updateResource(r,tronAccountService,ownerAccount,tronTransactionsService));
                });

    }
    public boolean filter(Contract.UnDelegateResourceContract unDelegateResourceContract ,Map<String, TronAccount> accountMap ){
        return accountMap.containsKey(Base58Check.bytesToBase58(unDelegateResourceContract.getOwnerAddress().toByteArray()))  ||
                accountMap.containsKey(Base58Check.bytesToBase58(unDelegateResourceContract.getReceiverAddress().toByteArray()));
    }

}
