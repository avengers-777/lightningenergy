package com.alameda.lightningenergy.entity.data;

import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionData {
    private Contract.TransferContract transferContract;
    private Contract.AccountCreateContract accountCreateContract;
    private Contract.AccountPermissionUpdateContract accountPermissionUpdateContract;
    private Contract.FreezeBalanceV2Contract freezeBalanceV2Contract;
    private Contract.UnfreezeBalanceV2Contract unfreezeBalanceV2Contract;
    private Contract.DelegateResourceContract delegateResourceContract;
    private Contract.UnDelegateResourceContract unDelegateResourceContract;
    private Contract.WithdrawExpireUnfreezeContract withdrawExpireUnfreezeContract;
    private Contract.CancelAllUnfreezeV2Contract cancelAllUnfreezeV2Contract;
    private Response.TransactionExtention transactionExtention;
    private Response.TransactionInfo transactionInfo;

    public Flux<ByteString> getTransferContractAddress(){

        return Mono.justOrEmpty(transferContract).filter(Objects::nonNull).flatMapMany(t -> Flux.just(t.getOwnerAddress(),t.getToAddress()));

    }

    public Flux<ByteString> getAccountCreateContractAddress(){
        return Mono.justOrEmpty(accountCreateContract).filter(Objects::nonNull).flatMapMany(a -> Flux.just(a.getOwnerAddress(),a.getAccountAddress()));

    }
    public Flux<ByteString> getAccountPermissionUpdateContractAddress(){
        return Mono.justOrEmpty(accountPermissionUpdateContract).filter(Objects::nonNull).flatMapMany(a -> {
            return Flux.fromIterable(a.getActivesList())
                    .flatMap(permission -> Flux.fromIterable(permission.getKeysList())) // 将内层集合转换为 Stream 并进行扁平化
                    .map(Common.Key::getAddress) ;
        });

    }
    public Mono<ByteString> getFreezeBalanceV2ContractAddress(){
        return Mono.justOrEmpty(freezeBalanceV2Contract).filter(Objects::nonNull).map(Contract.FreezeBalanceV2Contract::getOwnerAddress);
    }
    public Mono<ByteString> getUnfreezeBalanceV2ContractAddress(){
        return Mono.justOrEmpty(unfreezeBalanceV2Contract).filter(Objects::nonNull).map(Contract.UnfreezeBalanceV2Contract::getOwnerAddress);
    }
    public Flux<ByteString> getDelegateResourceContractAddress(){
        return Mono.justOrEmpty(delegateResourceContract).filter(Objects::nonNull).flatMapMany(d -> Flux.just(d.getOwnerAddress(),d.getReceiverAddress()));
    }
    public Flux<ByteString> getUnDelegateResourceContractAddress(){
        return Mono.justOrEmpty(unDelegateResourceContract).filter(Objects::nonNull).flatMapMany(u -> {
            return Flux.just(u.getOwnerAddress(),u.getReceiverAddress());
        });


    }
    public Mono<ByteString> getWithdrawExpireUnfreezeContractAddress(){
        return Mono.justOrEmpty(withdrawExpireUnfreezeContract).filter(Objects::nonNull).map(Contract.WithdrawExpireUnfreezeContract::getOwnerAddress);
    }
    public Mono<ByteString> getCancelAllUnfreezeV2ContractAddress(){
        return Mono.justOrEmpty(cancelAllUnfreezeV2Contract).filter(Objects::nonNull).map(Contract.CancelAllUnfreezeV2Contract::getOwnerAddress);
    }
    public Flux<String> getAllAddress(){
        return Flux.merge(
                getTransferContractAddress(),
                getAccountCreateContractAddress(),
                getAccountPermissionUpdateContractAddress(),
                getFreezeBalanceV2ContractAddress(),
                getUnfreezeBalanceV2ContractAddress(),
                getDelegateResourceContractAddress(),
                getUnDelegateResourceContractAddress(),
                getWithdrawExpireUnfreezeContractAddress(),
                getCancelAllUnfreezeV2ContractAddress()
        )
                .filter(Objects::nonNull)
                .map(ByteString::toByteArray)
                .filter(Objects::nonNull)
                .map(Base58Check::bytesToBase58);


    }


}
