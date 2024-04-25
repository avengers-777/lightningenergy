package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.config.security.RASUtils;
import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Service
@Slf4j
public class TronScanBlockServiceImpl {

    private final ApiWrapper apiWrapper;
    private final RASUtils rasUtils;
    private final RateLimiterService rateLimiterService;
    private final TronAccountServiceImpl tronAccountService;
    private final TronTransactionsServiceImpl tronTransactionsService;
    private final Environment environment;
    @Getter
    @Setter
    private Boolean enableScanBlock = true;

//    @PostConstruct
    public void initScanBlockHeight() {
        getBlock().subscribe(block -> {
            long height = block.getBlockHeader().getRawData().getNumber();
            long timestamp = block.getBlockHeader().getRawData().getTimestamp();
            rateLimiterService.saveTronBlockHeight(height).subscribe();
            rateLimiterService.saveTronBlockHeightRecord(height,timestamp).subscribe();
        });
    }





    public Mono<Tuple2<Response.BlockExtention, Response.TransactionInfoList>> getBlockDataByNumber(long height) {
        try {

            Response.BlockExtention block = apiWrapper.getBlockByNum(height);
            if (block.getTransactionsCount() > 0) {
                Response.TransactionInfoList list = apiWrapper.getTransactionInfoByBlockNum(height);
                Tuple2<Response.BlockExtention, Response.TransactionInfoList> data = Tuples.of(block,list);
                return Mono.just(data);
            } else {
                return Mono.just(Tuples.of(block,Response.TransactionInfoList.getDefaultInstance()));
            }

        } catch (IllegalException e) {
            return Mono.error(e);
        }
    }

    public Flux<TronTransactionRecord> blockDateHandler(Tuple2<Response.BlockExtention, Response.TransactionInfoList> tuple2) {
        Response.TransactionInfoList transactionInfoList = tuple2.getT2();

        Flux<Response.TransactionExtention> transactionExtentionFlux = Flux.fromIterable(tuple2.getT1().getTransactionsList())
                .filter(transactionExtention -> transactionExtention.getTransaction().getRet(0).getContractRet().equals(Chain.Transaction.Result.contractResult.SUCCESS))
                .filter(transactionExtention -> transactionExtention.getResult().getResult()).share();

        return tronTransactionsService.blockEventMainHandler(transactionInfoList,transactionExtentionFlux);




    }




    public Mono<Chain.Block> getBlock() {
        return Mono.defer(()->{
            try {
                Chain.Block block = apiWrapper.getNowBlock();
                return Mono.justOrEmpty(block);
            } catch (IllegalException e) {
                return Mono.error(e);
            }
        });


    }


}
