package com.alameda.lightningenergy.entity.dto;


import com.alameda.lightningenergy.entity.enums.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Common;
import reactor.core.publisher.Mono;

import java.util.Set;

import static com.alameda.lightningenergy.entity.common.Constants.MIN_BANDWIDTH_USAGE;
import static com.alameda.lightningenergy.entity.common.Constants.MIN_ENERGY_USAGE;
import static com.alameda.lightningenergy.utils.BlockUtils.THIRTY_DAYS_IN_MILLIS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TronResourceRentalRequest {
    private String deviceId;
    private Common.ResourceCode resourceCode;
    private Long amount;
    private Boolean lockup;
    private Long duration;
    private String receivingAddress;

    public static final Set<Common.ResourceCode> resourceCodeSet = Set.of(Common.ResourceCode.ENERGY,Common.ResourceCode.BANDWIDTH);

    public Mono<Boolean> verify()  {

        return Mono.just(this)
                .filter(request -> duration < THIRTY_DAYS_IN_MILLIS)
                .switchIfEmpty(Mono.error(ErrorType.DURATION_EXCEEDS_MAXIMUM.getException()))
                .filter(request -> resourceCodeSet.contains(resourceCode))
                .switchIfEmpty(Mono.error(ErrorType.UNSUPPORTED_RESOURCES.getException()))
                .filter(request -> (resourceCode.equals(Common.ResourceCode.BANDWIDTH) && amount >= MIN_BANDWIDTH_USAGE) || (resourceCode.equals(Common.ResourceCode.ENERGY)  && amount >= MIN_ENERGY_USAGE))
                .switchIfEmpty(Mono.error(ErrorType.PARAMETER_IS_ILLEGAL.getException()))
                .flatMap(request -> {
                    try{
                        ApiWrapper.parseAddress(receivingAddress);
                        return Mono.just(true);
                    }catch (Exception e){
                        return Mono.error(ErrorType.ADDRESS_VERIFICATION_ERROR.getException());
                    }
                });



    }


}
