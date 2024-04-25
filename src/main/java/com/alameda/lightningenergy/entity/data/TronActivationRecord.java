package com.alameda.lightningenergy.entity.data;


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
@Document("tron_activation_records")
@Builder
public class TronActivationRecord {
    @Id
    private String id;
    private TransactionType type;
    private String userId;
    private String ip;
    private String from;
    private String to;
    private String txid;
    private Long createDate;
    private Long updateDate;
    private String error;
    private TransferStatus status;

    public TronActivationRecord(TransactionType type, String userId, String ip, String from, String to){
        this.type = type;;
        this.userId = userId;
        this.ip = ip;
        this.from = from;;
        this.to = to;
        this.txid = txid;
        this.createDate = System.currentTimeMillis();
        this.updateDate = System.currentTimeMillis();
        this.status = TransferStatus.PROGRESS;
    }
    public TronActivationRecord(TronAccount oldAccount,TronAccount newAccount,String userId, String ip){
        this.type = TransactionType.INTERNAL;
        this.userId = userId;
        this.ip = ip;
        this.from = oldAccount.getId();
        this.to = newAccount.getId();
        this.createDate = System.currentTimeMillis();
        this.updateDate = System.currentTimeMillis();
        this.status = TransferStatus.PROGRESS;
    }
}
