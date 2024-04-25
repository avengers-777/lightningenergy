package com.alameda.lightningenergy.entity.data;


import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.ContractType;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.utils.OperationsEncoderAndDecoder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("tron_accounts")
public class TronAccount {
    @Id
    private String id;
    private String label;
    @Indexed
    private String base58CheckAddress;
    private Set<Permission> permissionSet;
    @JsonIgnore
    private String privateKey;
    @Indexed
    private Balance balance;
    private Long createDate;
    private Long updateDate;
    @Indexed
    private AccountType accountType;
    @Indexed
    private Status status;


    @Transient
    @JsonIgnore
    private KeyPair keyPair;
    @Transient
    @JsonIgnore
    private ApiWrapper apiWrapper;
    public static final String ADDRESS_PROPERTIES = "base58CheckAddress";
    public static final String ACCOUNT_TYPE_PROPERTIES = "accountType";
    public static final String PERMISSION_SET_PROPERTIES = "permissionSet";
    public static final String BALANCE_PROPERTIES = "balance";

    public enum Status {
        ACTIVE,
        INACTIVATED,
        DELETED,
        DISABLED, // 禁用

    }

    public TronAccount(String address,AccountType accountType){
        this.label =  address.substring(address.length() - 5);
        this.base58CheckAddress = address;
        this.permissionSet = new HashSet<>();
        this.balance = new Balance();
        this.createDate = System.currentTimeMillis();
        this.updateDate = System.currentTimeMillis();
        this.accountType = accountType;
        this.status = Status.ACTIVE;
    }

    public TronAccount(KeyPair keyPair, RASUtils rasUtils) throws Exception {
        this.base58CheckAddress = keyPair.toBase58CheckAddress();
        this.label = this.base58CheckAddress.substring(this.base58CheckAddress.length() - 5);
        this.privateKey = rasUtils.encrypt(keyPair.toPrivateKey());
        this.balance = new Balance();
        this.createDate = System.currentTimeMillis();
        this.updateDate = System.currentTimeMillis();
        this.status = Status.INACTIVATED;
        this.accountType = AccountType.INTERNAL;
        this.permissionSet = new HashSet<>();
    }
    public void init(RASUtils rasUtils,ApiWrapper apiWrapper) throws NoSuchPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String afterDecryptionPrivateKey = rasUtils.decrypt(privateKey);
        this.keyPair = new KeyPair(afterDecryptionPrivateKey);
        this.apiWrapper = apiWrapper;
    }

    public Mono<Integer> verifyPermission(List<Common.Permission> permissions){
        return Flux.fromIterable(permissions)
                .flatMap(permission -> filterKeys(permission).map(key -> Tuples.of(permission,key)))
                .next()
                .switchIfEmpty(Mono.error(ErrorType.ADDRESS_IS_NOT_AUTHORIZED.getException()))
                .filter(t -> t.getT1().getThreshold() <= t.getT2().getWeight())
                .switchIfEmpty(Mono.error(ErrorType.NOT_ENOUGH_WEIGHT.getException()))
                .map(t -> Tuples.of(t.getT1(),t.getT2(),OperationsEncoderAndDecoder.operationsDecoder(t.getT1().getOperations())))
                .filter(t -> t.getT3().contains(ContractType.DelegateResourceContract))
                .switchIfEmpty(Mono.error(ErrorType.NO_DELEGATE_RESOURCE_PERMISSIONS.getException()))
                .filter(t -> t.getT3().contains(ContractType.UnDelegateResourceContract))
                .switchIfEmpty(Mono.error(ErrorType.NO_UNDELEGATE_RESOURCE_PERMISSIONS.getException()))
                .map(t -> t.getT1().getId());

    }

    public Mono<Permission> pickOnePermission(){
        return Flux.fromIterable(permissionSet)
                .filter(permission -> permission.getThreshold() <= permission.getWeight())
                .switchIfEmpty(Mono.error(ErrorType.NOT_ENOUGH_WEIGHT.getException()))
                .filter(permission -> permission.getOperations().contains(ContractType.DelegateResourceContract))
                .switchIfEmpty(Mono.error(ErrorType.NO_DELEGATE_RESOURCE_PERMISSIONS.getException()))
                .filter(permission -> permission.getOperations().contains(ContractType.UnDelegateResourceContract))
                .switchIfEmpty(Mono.error(ErrorType.NO_UNDELEGATE_RESOURCE_PERMISSIONS.getException()))
                .next();
    }

    public Tuple2<Response.TransactionExtention, Chain.Transaction> transfer(String toAddress, long amount) throws IllegalException {
        Response.TransactionExtention transaction = apiWrapper.transfer(base58CheckAddress, toAddress, amount);
        Chain.Transaction signedTxn = apiWrapper.signTransaction(transaction, keyPair);
        return Tuples.of(transaction,signedTxn);
    }

    public Tuple2<Response.TransactionExtention, Chain.Transaction> delegateResource(String ownerAddress,long balance, Common.ResourceCode resourceType, String receiverAddress, Boolean lock, long lockPeriod) throws  IllegalException {
        Response.TransactionExtention transactionExtention = apiWrapper.delegateResourceV2(ownerAddress,balance,resourceType.getNumber(),receiverAddress,lock,lockPeriod);
        Chain.Transaction signedTxn = apiWrapper.signTransaction(transactionExtention,keyPair);
        return Tuples.of(transactionExtention,signedTxn);
    }

    public Mono<Common.Key> filterKeys(Common.Permission permission){
        return Flux.fromIterable(permission.getKeysList())
                .filter(key -> {
                    String address = Base58Check.bytesToBase58(key.getAddress().toByteArray());
                    return address.equals(base58CheckAddress);
                })
                .next();
    }
    public Mono<Response.Account> verifyBalance(Response.Account account, Common.ResourceCode resourceCode, long amount){
        return Flux.fromIterable(account.getFrozenV2List())
                .filter(frozen -> frozen.getType().equals(resourceCode))
                .next()
                .map(frozen -> frozen.getAmount() > amount)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(ErrorType.LACK_OF_RESOURCES.getException()))
                .thenReturn(account);

    }
    public Mono<Tuple2<Response.TransactionExtention, Chain.Transaction>> delegateResourceFromAuthorizedAccount(TronAccount lessorAccount, String receivingAddress ,TronResourceRentalOrder rentalOrder, Permission permission) {
        try {
            Response.TransactionExtention transactionExtention = apiWrapper.delegateResourceV2(lessorAccount.getBase58CheckAddress(),rentalOrder.getValueInTrx(),rentalOrder.getResourceCode().getNumber(),receivingAddress,rentalOrder.getLockup(),rentalOrder.getDuration() / 3000);
            return Mono.just(multipleSignatures(transactionExtention,permission.getPermissionId()));
        } catch (IllegalException e) {
            return Mono.error(e);
        }
    }
    public Mono<Tuple2<Response.TransactionExtention, Chain.Transaction>> unDelegateResourceFromAuthorizedAccount(String ownerAddress, long amount, Common.ResourceCode resourceType, String receiverAddress,int permissionId) {
        try {
            Response.TransactionExtention transactionExtention = apiWrapper.undelegateResource(ownerAddress,amount,resourceType.getNumber(),receiverAddress);
            return Mono.just(multipleSignatures(transactionExtention,permissionId));
        } catch (IllegalException e) {
            return Mono.error(e);
        }
    }
    public Tuple2<Response.TransactionExtention, Chain.Transaction> multipleSignatures(Response.TransactionExtention transactionExtention, @NonNull Integer permissionId){
        Chain.Transaction.Builder transactionBuilder = transactionExtention.getTransaction().toBuilder();
        transactionBuilder.getRawDataBuilder().getContractBuilder(0).setPermissionId(permissionId);
        Chain.Transaction signedTxn = apiWrapper.signTransaction(transactionBuilder.build(),keyPair);
        return Tuples.of(transactionExtention,signedTxn);
    }

    public Tuple2<Response.TransactionExtention, Chain.Transaction> unelegateResource(String ownerAddress, long balance, Common.ResourceCode resourceType, String receiverAddress) throws IllegalException {
        Response.TransactionExtention transactionExtention = apiWrapper.undelegateResource(ownerAddress,balance,resourceType.getNumber(),receiverAddress);
        Chain.Transaction signedTxn = apiWrapper.signTransaction(transactionExtention, keyPair);
        return Tuples.of(transactionExtention,signedTxn);
    }
    public Tuple2<Response.TransactionExtention, Chain.Transaction> createAccount(String accountAddress) throws IllegalException {
        Response.TransactionExtention transaction = apiWrapper.createAccount(base58CheckAddress,accountAddress);
        Chain.Transaction signedTxn = apiWrapper.signTransaction(transaction, keyPair);
        return Tuples.of(transaction,signedTxn);
    }



}
