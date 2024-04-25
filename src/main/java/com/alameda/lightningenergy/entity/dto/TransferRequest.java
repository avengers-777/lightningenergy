package com.alameda.lightningenergy.entity.dto;

import com.alameda.lightningenergy.entity.enums.Currency;
import com.alameda.lightningenergy.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private TransactionType type;
    private Currency currency;
    private long amount;
    private String to;
    private String from;
}
