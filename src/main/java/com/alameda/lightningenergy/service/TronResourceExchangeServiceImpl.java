package com.alameda.lightningenergy.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RequiredArgsConstructor
@Service
@Slf4j
public class TronResourceExchangeServiceImpl {
    private final ApiWrapper apiWrapper;
    private final Environment environment;
    @Getter
    private Response.AccountResourceMessage accountResourceMessage;

    public Long resourceToTrx(Common.ResourceCode resourceCode,long amount){
        return  resourceCode.equals(Common.ResourceCode.ENERGY) ? energyToTrx(amount) : bandwidthToTrx(amount);
    }
    public Long trxToResource(Common.ResourceCode resourceCode,long amount){
        return resourceCode.equals(Common.ResourceCode.ENERGY) ? trxToEnergy(amount) : trxToBandwidth(amount);
    }
    public Long trxToEnergy(Long sunAmount) {
        BigDecimal amount = new BigDecimal(sunAmount).divide(new BigDecimal("1000000"), 10, RoundingMode.HALF_UP);
        BigDecimal totalEnergyWeight = new BigDecimal(accountResourceMessage.getTotalEnergyWeight());
        BigDecimal totalEnergyLimit = new BigDecimal(accountResourceMessage.getTotalEnergyLimit());

        BigDecimal result = amount.divide(totalEnergyWeight, 10, RoundingMode.HALF_UP)
                .multiply(totalEnergyLimit);

        return result.setScale(0, RoundingMode.HALF_UP).longValue();
    }


    public Long trxToBandwidth(Long sunAmount) {
        BigDecimal amount = new BigDecimal(sunAmount).divide(new BigDecimal("1000000"), 10, RoundingMode.HALF_UP);
        BigDecimal totalNetWeight = new BigDecimal(accountResourceMessage.getTotalNetWeight());
        BigDecimal totalNetLimit = new BigDecimal(accountResourceMessage.getTotalNetLimit());

        BigDecimal result = amount.divide(totalNetWeight, 10, RoundingMode.HALF_UP)
                .multiply(totalNetLimit);

        return result.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public Long bandwidthToTrx(Long amount) {
        BigDecimal amountBD = new BigDecimal(amount);
        BigDecimal totalNetLimit = new BigDecimal(accountResourceMessage.getTotalNetLimit());
        BigDecimal totalNetWeight = new BigDecimal(accountResourceMessage.getTotalNetWeight());

        BigDecimal result = amountBD.divide(totalNetLimit, 10, RoundingMode.HALF_UP)
                .multiply(totalNetWeight)
                .multiply(new BigDecimal("1000000"));

        return result.setScale(0, RoundingMode.HALF_UP).longValue();
    }
    public Long energyToTrx(Long amount) {
        BigDecimal amountBD = new BigDecimal(amount);
        BigDecimal totalEnergyLimit = new BigDecimal(accountResourceMessage.getTotalEnergyLimit());
        BigDecimal totalEnergyWeight = new BigDecimal(accountResourceMessage.getTotalEnergyWeight());

        BigDecimal result = amountBD.divide(totalEnergyLimit, 10, RoundingMode.HALF_UP)
                .multiply(totalEnergyWeight)
                .multiply(new BigDecimal("1000000"));

        return result.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    @PostConstruct
    public void updateAccountResourceMessage(){
        accountResourceMessage = apiWrapper.getAccountResource(environment.getProperty("tron.default-address"));
    }




}
