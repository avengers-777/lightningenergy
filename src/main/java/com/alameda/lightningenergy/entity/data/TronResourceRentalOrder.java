package com.alameda.lightningenergy.entity.data;

import com.alameda.lightningenergy.entity.dto.TronResourceRentalRequest;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.service.TronResourceExchangeServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.tron.trident.proto.Common;

import static com.alameda.lightningenergy.utils.BlockUtils.THIRTY_DAYS_IN_MILLIS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("tron_resource_rental_orders")
public class TronResourceRentalOrder {
    @Id
    private String id;
    private String depositOrderId;
    @Indexed
    private String receivingAccountId;

    @Indexed
    private String lessorId;
    @Indexed
    private String deviceId;
    private String ip;
    @Indexed
    private Common.ResourceCode resourceCode;
    @Indexed
    private Long valueInTrx;
    @Indexed
    private Long amount;
    private Boolean lockup;
    @Indexed
    private Long duration;
    @Indexed
    private Long expectedReclaimTime;
    private Long expectedReclaimBlockHeight;
    private String error;
    private String txid;
    private String reclaimTxid;
    @Indexed
    private Long createDate;
    @Indexed
    private Long transactionTime;
    @Indexed
    private Long completeTime;
    @Indexed
    private Status status;
    public static final String LESSOR_ID_PROPERTIES = "lessorId";
    public static final String EXPECTED_RECLAIM_TIME_PROPERTIES = "expectedReclaimTime";
    public static final String EXPECTED_RECLAIM_BLOCK_HEIGHT_PROPERTIES= "expectedReclaimBlockHeight";
    public static final String TRANSACTION_TIME_PROPERTIES = "transactionTime";
    public static  final String RECLAIM_TXID_PROPERTIES = "reclaimTxid";
    public static final String COMPLETE_TIME_PROPERTIES = "completeTime";
    public static final String DEPOSIT_ORDER_ID_PROPERTIES = "depositOrderId";

    public TronResourceRentalOrder(TronResourceExchangeServiceImpl tronResourceExchangeService,TronResourceRentalRequest tronResourceRentalRequest, String ip, String accountId) throws ErrorType.ApplicationException {
        if (tronResourceRentalRequest.getDuration() > THIRTY_DAYS_IN_MILLIS){
            throw ErrorType.DURATION_EXCEEDS_MAXIMUM.getException();
        }
        this.receivingAccountId = accountId;
        this.deviceId = tronResourceRentalRequest.getDeviceId();
        this.ip = ip;
        this.resourceCode = tronResourceRentalRequest.getResourceCode();
        this.valueInTrx = tronResourceExchangeService.resourceToTrx(tronResourceRentalRequest.getResourceCode(), tronResourceRentalRequest.getAmount());
        this.amount = tronResourceRentalRequest.getAmount();
        this.lockup = tronResourceRentalRequest.getLockup();
        this.duration = tronResourceRentalRequest.getDuration();
        this.expectedReclaimTime = 0L;
        this.expectedReclaimBlockHeight = 0L;
        this.createDate = System.currentTimeMillis();
        this.transactionTime = 0L;
        this.completeTime = 0L;
        this.status = Status.PROGRESS;
    }

    public enum Status {
        PROGRESS,
        PENDING_RECLAIM,
        COMPLETED,
        ERROR,
        RECLAIM_FAILED
    }


}
