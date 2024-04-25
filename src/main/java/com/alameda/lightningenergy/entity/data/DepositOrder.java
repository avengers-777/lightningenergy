package com.alameda.lightningenergy.entity.data;

import com.alameda.lightningenergy.entity.enums.BlockchainPlatform;
import com.alameda.lightningenergy.entity.enums.Currency;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.utils.ResourcePriceCalculator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("deposit_orders")
public class DepositOrder {
    @Id
    private String id;
    @Indexed
    private String deviceId;
    private BlockchainPlatform platform;
    @Indexed
    private String receivingAccountId;
    private String fromAddress;
    private Currency currency;
    @Indexed
    private Long amount;
    private String txid;
    private Long blockHeight;
    private Long createDate;
    private Long receivingDate;
    @Indexed
    private Long expirationDate;
    @Indexed
    private List<String> actions;
    @Indexed
    private DepositStatus status;
    public static final String EXPIRATION_DATE_PROPERTIES = "expirationDate";
    public static final String AMOUNT_PROPERTIES = "amount";
    public static final String CURRENCY_PROPERTIES = "currency";
    public static final String PLATFORM_PROPERTIES = "platform";
    public static final String RECEIVING_ACCOUNT_ID_PROPERTIES = "receivingAccountId";
    public static final String FROM_ADDRESS_PROPERTIES = "fromAddress";
    public static final String BLOCK_HEIGHT_PROPERTIES = "blockHeight";
    public static final String RECEIVING_DATE_PROPERTIES = "receivingDate";



    public DepositOrder(List<TronResourceRentalOrder> tronResourceRentalOrders) throws ErrorType.ApplicationException {
        if (tronResourceRentalOrders == null || tronResourceRentalOrders.isEmpty()) {
            throw ErrorType.LIST_NOT_EMPTY.getException();
        }

        List<String> actionList = new ArrayList<>();
        this.amount = 0L;
        for (TronResourceRentalOrder order : tronResourceRentalOrders) {
            long price = ResourcePriceCalculator.calculateResourcePricePerUnit(order.getResourceCode(), order.getDuration());
            long totalResources = ResourcePriceCalculator.calculateTotalResources(order.getAmount(), order.getDuration());
            actionList.add(order.getId());
            this.amount += ResourcePriceCalculator.toNearestBase(price * totalResources,BlockchainPlatform.TRON.getZeros());
        }


        this.deviceId = tronResourceRentalOrders.getFirst().getDeviceId();
        this.platform = BlockchainPlatform.TRON;
        this.currency = Currency.TRX;
        this.createDate = System.currentTimeMillis();
        this.expirationDate = System.currentTimeMillis() + Duration.ofMinutes(10).toMillis();
        this.actions = actionList;
        this.status = DepositStatus.PENDING;
    }

    public DepositOrder updateReceivingAccount(TronAccount tronAccount){
        this.receivingAccountId = tronAccount.getId();
        if (tronAccount.getStatus().equals(TronAccount.Status.INACTIVATED)){
            setAmount(amount - currency.calculateValue(1));
        }
        return this;
    }

    public enum DepositStatus {
        PENDING,
        RECEIVED
    }


}
