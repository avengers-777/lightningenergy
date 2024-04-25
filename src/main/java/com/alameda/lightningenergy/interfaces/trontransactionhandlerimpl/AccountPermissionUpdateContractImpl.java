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
import java.util.Objects;
import java.util.stream.Stream;

public class AccountPermissionUpdateContractImpl extends TronTransactionHandler {


    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.AccountPermissionUpdateContract result = Contract.AccountPermissionUpdateContract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention, transactionInfo, contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.getTransactionData().setAccountPermissionUpdateContract(result);
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
        Contract.AccountPermissionUpdateContract data = record.getTransactionData().getAccountPermissionUpdateContract();
        return Mono.justOrEmpty(data).filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()))
                .filter(accountPermissionUpdateContract -> {
                    Stream<String> activesStream = accountPermissionUpdateContract.getActivesList().stream().flatMap(permission -> permission.getKeysList().stream()).map(key -> key.getAddress().toByteArray()).map(Base58Check::bytesToBase58);
                    Stream<String> ownerStream = accountPermissionUpdateContract.getOwner().getKeysList().stream().map(key -> key.getAddress().toByteArray()).map(Base58Check::bytesToBase58);
                    return Stream.concat(activesStream, ownerStream).anyMatch(accountMap::containsKey);
                })
                .flatMap(accountPermissionUpdateContract ->
                        tronAccountService.authorizedAccountBlockDataHandler(record, accountMap, data))

                .flatMap(tronAccountService::saveTronAccount)
                .map(record::update)
                .doOnNext(r -> {
                    super.saveSuccessRecord(r,tronTransactionsService).subscribe();
                })
                .doOnError(e -> {
                    super.saveErrorRecord(record,tronTransactionsService,e.getMessage()).subscribe();
                });
    }
}
