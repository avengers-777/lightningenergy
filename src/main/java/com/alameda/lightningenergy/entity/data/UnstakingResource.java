package com.alameda.lightningenergy.entity.data;

import com.alameda.lightningenergy.utils.BlockUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Response;

import static com.alameda.lightningenergy.utils.BlockUtils.BLOCK_TIME_MILLIS;
import static com.alameda.lightningenergy.utils.BlockUtils.UNLOCK_BLOCK_COUNT;

@Data
@NoArgsConstructor
@Builder
public class UnstakingResource {
    private String txid;
    private Common.ResourceCode resourceType;
    private long unfreezeAmount;
    private long withdrawalAvailableAt;
    private long unlockBlockHeight;

    public UnstakingResource(Response.Account.UnFreezeV2 unFreezeV2,long blockHeightRecord, long updateTimeRecord){
        this.resourceType = unFreezeV2.getType();
        this.unfreezeAmount = unFreezeV2.getUnfreezeAmount();
        this.withdrawalAvailableAt = unFreezeV2.getUnfreezeExpireTime();
        this.unlockBlockHeight = BlockUtils.getBlockHeightByTimestamp(blockHeightRecord,updateTimeRecord,unFreezeV2.getUnfreezeExpireTime());
    }
    public UnstakingResource(String txid, Common.ResourceCode resourceType, long unfreezeAmount, long currentBlockHeight, long currentBlockTimestamp) {
        this.txid = txid;
        this.resourceType = resourceType;
        this.unlockBlockHeight = currentBlockHeight + UNLOCK_BLOCK_COUNT;
        this.unfreezeAmount = unfreezeAmount;
        long unlockTimeMillis = UNLOCK_BLOCK_COUNT * BLOCK_TIME_MILLIS;
        this.withdrawalAvailableAt = currentBlockTimestamp + unlockTimeMillis;
    }
    public boolean isUnlocked(long currentBlockHeight){
        return currentBlockHeight >= unlockBlockHeight;
    }



}
