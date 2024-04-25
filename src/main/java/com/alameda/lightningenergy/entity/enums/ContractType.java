package com.alameda.lightningenergy.entity.enums;

import com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl.TransferContractImpl;
import com.alameda.lightningenergy.interfaces.TronTransactionHandler;
import com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl.UnfreezeBalanceContractImpl;
import com.alameda.lightningenergy.interfaces.trontransactionhandlerimpl.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ContractType {
    AccountCreateContract(0,new AccountCreateContractImpl()),
    TransferContract(1,new TransferContractImpl()),
    TransferAssetContract(2,new TransferAssetContractImpl()),
    VoteAssetContract(3,new VoteAssetContractImpl()),
    VoteWitnessContract(4,new VoteWitnessContractImpl()),
    WitnessCreateContract(5,new WitnessCreateContractImpl()),
    AssetIssueContract(6,new AssetIssueContractImpl()),
    WitnessUpdateContract(8,new WitnessUpdateContractImpl()),
    ParticipateAssetIssueContract(9,new ParticipateAssetIssueContractImpl()),
    AccountUpdateContract(10,new AccountUpdateContractImpl()),
    FreezeBalanceContract(11,new FreezeBalanceContractImpl()),
    UnfreezeBalanceContract(12,new UnfreezeBalanceContractImpl()),
    WithdrawBalanceContract(13,new WithdrawBalanceContractImpl()),
    UnfreezeAssetContract(14,new UnfreezeAssetContractImpl()),
    UpdateAssetContract(15,new UpdateAssetContractImpl()),
    ProposalCreateContract(16,new ProposalCreateContractImpl()),
    ProposalApproveContract(17,new ProposalApproveContractImpl()),
    ProposalDeleteContract(18,new ProposalDeleteContractImpl()),
    SetAccountIdContract(19,new SetAccountIdContractImpl()),
    CustomContract(20,new DefaultImpl()),
    CreateSmartContract(30,new CreateSmartContractImpl()),
    TriggerSmartContract(31,new TriggerSmartContractImpl()),
    GetContract(32,new DefaultImpl()),
    UpdateSettingContract(33,new UpdateSettingContractImpl()),
    ExchangeCreateContract(41,new ExchangeCreateContractImpl()),
    ExchangeInjectContract(42,new ExchangeInjectContractImpl()),
    ExchangeWithdrawContract(43,new ExchangeWithdrawContractImpl()),
    ExchangeTransactionContract(44,new ExchangeTransactionContractImpl()),
    UpdateEnergyLimitContract(45,new UpdateEnergyLimitContractImpl()),
    AccountPermissionUpdateContract(46,new AccountPermissionUpdateContractImpl()),
    ClearABIContract(48,new ClearABIContractImpl()),
    UpdateBrokerageContract(49,new UpdateBrokerageContractImpl()),
    ShieldedTransferContract(51,new DefaultImpl()),
    MarketSellAssetContract(52,new MarketSellAssetContractImpl()),
    MarketCancelOrderContract(53,new MarketCancelOrderContractImpl()),
    FreezeBalanceV2Contract(54,new FreezeBalanceV2ContractImpl()),
    UnfreezeBalanceV2Contract(55,new UnfreezeBalanceV2ContractImpl()),
    WithdrawExpireUnfreezeContract(56,new WithdrawExpireUnfreezeContractImpl()),
    DelegateResourceContract(57,new DelegateResourceContractImpl()),
    UnDelegateResourceContract(58,new UnDelegateResourceContractImpl()),
    CancelAllUnfreezeV2Contract(59,new CancelAllUnfreezeV2ContractImpl()),
    UNRECOGNIZED(-1,new DefaultImpl());
    private final int num;
    private final TronTransactionHandler handler;
    private static final Map<Integer, ContractType> NUM_TO_TYPE_MAP = new HashMap<>();

    static {
        for (ContractType type : ContractType.values()) {
            NUM_TO_TYPE_MAP.put(type.num, type);
        }
    }

    ContractType(int num,TronTransactionHandler handler) {
        this.num = num;
        this.handler = handler;
    }



    public static ContractType getContractTypeByNum(int num) {
        return NUM_TO_TYPE_MAP.getOrDefault(num, ContractType.UNRECOGNIZED);
    }}
