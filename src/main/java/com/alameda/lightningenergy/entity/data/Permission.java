package com.alameda.lightningenergy.entity.data;

import com.alameda.lightningenergy.entity.enums.AccountType;
import com.alameda.lightningenergy.entity.enums.ContractType;
import com.alameda.lightningenergy.utils.OperationsEncoderAndDecoder;
import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Contract;
import org.tron.trident.utils.Base58Check;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private String authorizedTo;
    private AuthorizedType authType;
    private Long threshold;
    private Long weight;
    private Set<ContractType> operations;
    private Integer permissionId;
    private String txid;
    private Long blockHeight;
    private Long blockTimeStamp;
    private Long updateDate;

    public static final String OPERATIONS_PROPERTIES = "permissionSet.operations";

    public enum AuthorizedType {
        OWNER,
        ACTIVES

    }
    public static Flux<Permission> activesListHandler(Contract.AccountPermissionUpdateContract accountPermissionUpdateContract, TronTransactionRecord record, Map<String,TronAccount> accountMap){
        return Flux.fromIterable(accountPermissionUpdateContract.getActivesList())
                .flatMapIterable(actives -> actives.getKeysList().stream()
                        .filter(key -> Permission.keyFilter(key,accountMap))
                        .map(key -> Tuples.of(actives, key))
                        .collect(Collectors.toList()))

                .map(activesKeyTuple -> {
                    Common.Permission actives = activesKeyTuple.getT1();
                    Common.Key key = activesKeyTuple.getT2();
                    TronAccount tronAccount = accountMap.get(Base58Check.bytesToBase58(key.getAddress().toByteArray()));
                    return new Permission(
                            tronAccount.getId(),
                            Permission.AuthorizedType.ACTIVES,
                            actives.getThreshold(),
                            key.getWeight(),
                            actives.getId(),
                            actives.getOperations(),
                            record.getTxid(),
                            record.getBlockHeight(),
                            record.getBlockTimeStamp()
                    );
                });
    }
    public static boolean keyFilter(Common.Key key,Map<String,TronAccount> accountMap){

        String address = Base58Check.bytesToBase58(key.getAddress().toByteArray());
        return Optional.ofNullable(accountMap.get(address)).map(TronAccount::getAccountType)
                .map(accountType -> accountType.equals(AccountType.INTERNAL))
                .orElse(false);
    }
    public static Flux<Permission> ownerPermissionHandler(Contract.AccountPermissionUpdateContract accountPermissionUpdateContract, TronTransactionRecord record, Map<String,TronAccount> accountMap){
        Common.Permission ownerPermission = accountPermissionUpdateContract.getOwner();
        return Flux.fromIterable(ownerPermission.getKeysList())
                .filter(key -> Permission.keyFilter(key,accountMap))
                .map(key->{
                    TronAccount tronAccount = accountMap.get(Base58Check.bytesToBase58(key.getAddress().toByteArray()));
                    return new Permission(
                            tronAccount.getId(),
                            Permission.AuthorizedType.OWNER,
                            ownerPermission.getThreshold(),
                            key.getWeight(),
                            0,
                            record.getTxid(),
                            record.getBlockHeight(),
                            record.getBlockTimeStamp()
                    );
                });
    }
    public Permission(String authorizedTo,  AuthorizedType authType, Long threshold, Long weight, Integer permissionId, String txid,Long blockHeight,Long blockTimeStamp){
        this(authorizedTo,authType,threshold,weight,permissionId,new HashSet<>(),txid,blockHeight,blockTimeStamp);
    }
    public Permission(String authorizedTo,  AuthorizedType authType, Long threshold, Long weight, Integer permissionId,ByteString operations, String txid,Long blockHeight,Long blockTimeStamp){
        this(authorizedTo,authType,threshold,weight,permissionId,OperationsEncoderAndDecoder.operationsDecoder(operations),txid,blockHeight,blockTimeStamp);
    }
    private Permission(String authorizedTo,  AuthorizedType authType, Long threshold, Long weight, Integer permissionId,Set<ContractType> operations, String txid,Long blockHeight,Long blockTimeStamp){
        this.authorizedTo = authorizedTo;
        this.authType = authType;
        this.threshold = threshold;
        this.weight = weight;
        this.operations = operations;
        this.permissionId = permissionId;
        this.txid = txid;
        this.updateDate = System.currentTimeMillis();
        this.blockHeight = blockHeight;
        this.blockTimeStamp = blockTimeStamp;
    }
}
