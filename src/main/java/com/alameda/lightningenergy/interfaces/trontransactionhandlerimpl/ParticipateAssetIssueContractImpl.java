package com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronAccount;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
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

public class ParticipateAssetIssueContractImpl extends TronTransactionHandler {



    @Override
    public TronTransactionRecord handler(Response.TransactionExtention transactionExtention, Response.TransactionInfo transactionInfo) throws InvalidProtocolBufferException {
        Chain.Transaction.Contract contract = super.getContract(transactionExtention);
        Contract.ParticipateAssetIssueContract result = Contract.ParticipateAssetIssueContract.parseFrom(super.getRawDataValue(contract));
        TronTransactionRecord tronTransactionRecord = super.getTransactionRecord(transactionExtention,transactionInfo,contract);
        String address = Base58Check.bytesToBase58(result.getOwnerAddress().toByteArray());
        String toAddress = Base58Check.bytesToBase58(result.getToAddress().toByteArray());
        tronTransactionRecord.setAddress(address);
        tronTransactionRecord.setExpense(result.getAmount());
        tronTransactionRecord.setToAddress(toAddress);
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
        return super.updateResource(record,tronAccountService,accountMap,tronTransactionsService);
    }




}
