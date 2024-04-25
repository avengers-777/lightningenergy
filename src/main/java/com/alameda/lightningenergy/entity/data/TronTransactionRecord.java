package com.alameda.lightningenergy.entity.data;

import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.ContractType;
import com.alameda.lightningenergy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.tron.trident.proto.Response;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("tron_transaction_records")
@Builder
public class TronTransactionRecord {
    @Id
    private String id;
    @Indexed
    private String accountId;
    private AccountType accountType;
    @Indexed
    private String address;
    @Indexed
    private String toAddress;
    private ContractType contractType;

    private Currency currency;
    private Long income;
    private Long expense;
    private Long fee;
    private Long energyFee;
    private Long energyUsage;
    private Long netFee;
    private Long netUsage;
    @Indexed
    private String txid;
    private Long blockHeight;
    private Long blockTimeStamp;
    private Long createDate;
    private String error;
    private Status status;
    @Transient
    @JsonIgnore
    private TransactionData transactionData;

    public static final String ACCOUNT_ID_PROPERTIES = "accountId";
    public static final String BLOCK_TIME_STAMP_PROPERTIES = "blockTimeStamp";


    public TronTransactionRecord update(@NonNull TronAccount account) {

        this.accountId = account.getId();
        this.accountType = account.getAccountType();

        return this;


    }

    public TronTransactionRecord(  Response.TransactionInfo transactionInfo,Response.TransactionExtention transactionExtention,ContractType contractType){
        this.contractType = contractType;
        this.accountType = AccountType.EXTERNAL;
        this.currency = Currency.TRX;
        this.income = 0L;
        this.expense = 0L;
        this.fee = transactionInfo.getFee();
        this.energyFee = transactionInfo.getReceipt().getEnergyFee();
        this.energyUsage = transactionInfo.getReceipt().getEnergyUsage();
        this.netFee = transactionInfo.getReceipt().getNetFee();
        this.netUsage = transactionInfo.getReceipt().getNetUsage();
        this.txid = Hex.toHexString(transactionInfo.getId().toByteArray());
        this.blockHeight = transactionInfo.getBlockNumber();
        this.blockTimeStamp = transactionInfo.getBlockTimeStamp();
        this.createDate = System.currentTimeMillis();
        this.status = Status.PROCESSING;
        this.transactionData = new TransactionData();
        this.transactionData.setTransactionExtention(transactionExtention);
        this.transactionData.setTransactionInfo(transactionInfo);
    }
    public enum Status{
        PROCESSING,
        COMPLETED,
        ERROR;
    }


}
