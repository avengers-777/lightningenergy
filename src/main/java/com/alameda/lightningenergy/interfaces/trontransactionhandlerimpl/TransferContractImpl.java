package com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
import com.alameda.lightningenergy.entity.enums.BlockchainPlatform;
import com.alameda.lightningenergy.entity.enums.Currency;
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

import java.time.Duration;
import java.util.Map;

public class TransferContractImpl extends TronTransactionHandler {


    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.TransferContract result = Contract.TransferContract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention, transactionInfo, contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        String toAddress = Base58Check.bytesToBase58(result.getToAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.setExpense(result.getAmount());
        tronTransactionRecord.setToAddress(toAddress);
        tronTransactionRecord.getTransactionData().setTransferContract(result);
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
        Contract.TransferContract data = record.getTransactionData().getTransferContract();
        return Mono.justOrEmpty(data)
                .switchIfEmpty(Mono.error(ErrorType.TRANSACTION_DATA_DOES_NOT_EXIST.getException()))
                .filter(transferContract -> filter(transferContract, accountMap))
                .doOnNext(transferContract -> {
                    transactionHandler(transferContract,tronResourceRentalOrderService,depositOrderService, record, accountMap).subscribe();
                })
                .flatMap(transferContract -> tronAccountTransferHandler(transferContract, accountMap,tronAccountService)
                        .flatMap(tronAccount -> updateBalance(transferContract,tronAccount,tronAccountService))
                        .flatMap(tronAccount -> updateStatus(tronAccount,tronAccountService))
                        .map(record::update)
                        .flatMap(r -> tronAccountService.updateResource(r).thenReturn(r))
                )
                .doOnNext(r -> super.saveSuccessRecord(r,tronTransactionsService).subscribe())
                .doOnError(e -> super.saveErrorRecord(record,tronTransactionsService,e.getMessage()).subscribe());
    }
    public Flux<String> transactionHandler(Contract.TransferContract transferContract, TronResourceRentalOrderServiceImpl tronResourceRentalOrderService, DepositOrderServiceImpl depositOrderService, TronTransactionRecord record, Map<String, TronAccount> accountMap){
        String ownerAddress = Base58Check.bytesToBase58(transferContract.getOwnerAddress().toByteArray());
        String toAddress = Base58Check.bytesToBase58(transferContract.getToAddress().toByteArray());
        Mono<TronAccount> toAddressMono = Mono.justOrEmpty(accountMap.get(toAddress));

        return toAddressMono.flatMapMany(tronAccount -> depositOrderService.findByOrder(transferContract.getAmount(), Currency.TRX, BlockchainPlatform.TRON,tronAccount.getId(),record.getBlockTimeStamp())
                .flatMap(depositOrder -> depositOrderService.updateDepositOrder(depositOrder.getId(),ownerAddress,record.getTxid(),record.getBlockHeight(),record.getBlockTimeStamp()).thenReturn(depositOrder))
                .flatMapMany(depositOrder -> tronResourceRentalOrderService.findByIds(depositOrder.getActions()))
                .delayElements(Duration.ofSeconds(3))
                .flatMap(rentalOrder -> tronResourceRentalOrderService.findCanDelegatedForResource(rentalOrder.getResourceCode(),rentalOrder.getValueInTrx())
                        .switchIfEmpty(Mono.error(ErrorType.INSUFFICIENT_DELEGABLE_RESOURCES.getException()))
                        .onErrorResume(e->tronResourceRentalOrderService.saveErrorStatus(e.getMessage(),rentalOrder.getId(),"").then(Mono.empty()))
                        .flatMap(lessorAccount-> tronResourceRentalOrderService.processResourceRentalDelegation(rentalOrder,lessorAccount))
                )
        );


    }
    public Mono<TronAccount> updateStatus(TronAccount tronAccount,TronAccountServiceImpl tronAccountService){
        if (tronAccount.getStatus().equals(TronAccount.Status.INACTIVATED)){
            return tronAccountService.updateStatus(tronAccount.getId(), TronAccount.Status.ACTIVE).thenReturn(tronAccount);
        }
        return Mono.just(tronAccount);
    }
    public Mono<TronAccount> updateBalance(Contract.TransferContract transferContract,TronAccount tronAccount,TronAccountServiceImpl tronAccountService){
        String ownerAddress =  Base58Check.bytesToBase58(transferContract.getOwnerAddress().toByteArray());
        long amount = tronAccount.getBase58CheckAddress().equals(ownerAddress) ? -transferContract.getAmount() : transferContract.getAmount();
        return tronAccountService.updateBalance(tronAccount.getId(),amount).thenReturn(tronAccount);
    }

    public Mono<TronAccount> tronAccountTransferHandler(Contract.TransferContract transferContract, Map<String, TronAccount> accountMap,TronAccountServiceImpl tronAccountService) {
        String ownerAddress = Base58Check.bytesToBase58(transferContract.getOwnerAddress().toByteArray());
        String toAddress = Base58Check.bytesToBase58(transferContract.getToAddress().toByteArray());
        Mono<TronAccount> toAddressMono = Mono.justOrEmpty(accountMap.get(toAddress)).flatMap(tronAccount -> updateBalance(transferContract,tronAccount,tronAccountService).then(updateStatus(tronAccount,tronAccountService)));
        Mono<TronAccount> ownerAddressMono = Mono.justOrEmpty(accountMap.get(ownerAddress));
        return toAddressMono.then(ownerAddressMono);

    }

    public boolean filter(Contract.TransferContract data, Map<String, TronAccount> accountMap) {
        String ownerAddress = Base58Check.bytesToBase58(data.getOwnerAddress().toByteArray());
        String toAddress = Base58Check.bytesToBase58(data.getToAddress().toByteArray());
        return accountMap.containsKey(ownerAddress) || accountMap.containsKey(toAddress);
    }


}
