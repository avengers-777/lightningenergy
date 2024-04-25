package com.alameda.lightningenergy.entity.enums;


import com.alameda.lightningenergy.entity.common.ResModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;

@Getter
@AllArgsConstructor
public enum ErrorType {
    NOT_LOGGED_IN_EXCEPTION(401,"Not logged in exception!"),
    THE_CHARACTER_IS_ABNORMAL(402,"The character is abnormal!"),
    PERMISSION_EXCEPTION(403,"Permission exception!"),
    ACCOUNT_BANNED(404,"Account banned!"),
    ERROR_INTERSECTIONALITIES(405,"Error NullPointerException!"),

    REGISTRATION_FAILED(1001,"Registration failed!"),
    WRONG_SIGNATURE_TYPE(1002, "Wrong Signature Type!"),
    NONCE_ERROR(1003, "Nonce Error!"),
    ABNORMAL_STATUS(1004,"Abnormal status!"),
    SIGNATURE_VERIFICATION_FAILED(1005, "Signature Verification Failed!"),
    ACCOUNT_DOES_NOT_EXIST(1006, "Account does not exist!"),
    ACCOUNT_ALREADY_EXISTS(1007, "Account already exists!"),
    ADDRESS_VERIFICATION_ERROR(1008, "Address verification error!"),
    NETWORK_ACCESS_FAILED(1009,"Network access failed!"),
    ADDRESS_IS_NOT_AUTHORIZED(1010,"Address is not authorized"),
    NOT_ENOUGH_WEIGHT(1011,"Not enough weight"),
    NO_DELEGATE_RESOURCE_PERMISSIONS(1012,"No Delegate Resource Permissions"),
    NO_UNDELEGATE_RESOURCE_PERMISSIONS(1013,"No UnDelegate Resource Permissions"),
    NO_VOTE_PERMISSIONS(1014,"No Vote Permissions"),
    LACK_OF_RESOURCES(1015,"Lack of Resources"),
    CANNOT_MODIFY_ADDRESS(1016,"Cannot Modify Address"),
    ACCOUNT_LOCKED(1017,"Account locked"),
    REDIS_LOCK_EXCEPTION(1018,"Redis Lock Exception"),
    REDIS_CACHE_EXCEPTION(1019,"Redis Cache Exception"),
    TRANSACTION_DATA_DOES_NOT_EXIST(1020,"Transaction data does not exist"),
    TRANSACTION_ALREADY_EXISTS(1021,"Transaction already exists"),
    DURATION_EXCEEDS_MAXIMUM(1022,"Duration exceeds the maximum limit of 30 days"),
    INSUFFICIENT_DELEGABLE_RESOURCES(1023, "Insufficient delegable resources, please try again later"),
    LIST_NOT_EMPTY(1024,"Rental orders list cannot be null or empty"),
    SYSTEM_BUSY(1025,"The system is very busy. Please try again later."),
    UNSUPPORTED_RESOURCES(1026,"Unsupported resources"),
    PARAMETER_IS_ILLEGAL(1027,"Illegal parameter"),
    AUTHORIZED_TO_ACCOUNT_DOES_NOT_EXIST(1028, "Authorization to account  does not exist"),
    SYSTEM_UNDER_MAINTENANCE(1029,"The system is currently under maintenance"),
    HEIGHT_LOCKED(1030,"Blockchain height has been locked"),
    UNKNOWN_ERROR(10000,"Unknown error!");

    private final Integer code;
    private final String msg;
    public ApplicationException getException() {
        return new ApplicationException(this.code, this.msg);
    }
    public static  <T> Mono<ResModel<T>> handleApplicationException(Throwable error) {
        return Mono.just(error)
                .filter(e -> e instanceof ApplicationException)
                .map(e -> ResModel.<T>error((ApplicationException) e))
                .defaultIfEmpty(ResModel.<T>error(ErrorType.UNKNOWN_ERROR));
    }

    @Getter
    public static class ApplicationException extends Exception{
        private final Integer code;
        public ApplicationException(Integer code,String msg){
            super(msg);
            this.code = code;
        }
    }

}
