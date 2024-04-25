package com.alameda.lightningenergy.entity.data;


import com.alameda.lightningenergy.entity.enums.Currency;
import com.alameda.lightningenergy.entity.enums.TransactionType;
import com.alameda.lightningenergy.entity.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("transfer_records")
@Builder
public class TransferRecord {
    @Id
    private String id;
    private TransactionType type;
    private String userId;
    private String ip;
    private Currency currency;
    private String from;
    private String to;
    private Long amount;
    private String txid;
    private Long createDate;
    private Long updateDate;
    private String error;
    private TransferStatus status;

    public TransferRecord(TransactionType type, String userId, String ip, Currency currency, String from, String to, long amount){
        this.type = type;
        this.userId = userId;
        this.ip = ip;
        this.currency = currency;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.createDate = System.currentTimeMillis();
        this.updateDate = System.currentTimeMillis();
        this.status = TransferStatus.PROGRESS;
    }

}
