package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.TronTransactionRecord;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class TronTransactionsMapper {

    private final ReactiveMongoTemplate template;
    public Mono<TronTransactionRecord> insert(TronTransactionRecord record){
        return template.insert(record);
    };
    public Flux<TronTransactionRecord> insertAll(List<TronTransactionRecord> accounts) {
        return template.insertAll(accounts);
    }
    public Flux<TronTransactionRecord> find(Query query){
        return template.find(query, TronTransactionRecord.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, TronTransactionRecord.class);
    }

    public Mono<TronTransactionRecord> save(TronTransactionRecord record){
        return template.save(record);
    }
    public Mono<TronTransactionRecord> findById(String id){
        return template.findById(id, TronTransactionRecord.class);
    }
    public Flux<TronTransactionRecord> findAll(){
        return template.findAll(TronTransactionRecord.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, TronTransactionRecord.class);
    }
    public Mono<TronTransactionRecord> findOne(Query query){
        return template.findOne(query, TronTransactionRecord.class);
    }
    public Mono<DeleteResult> removeObject(TronTransactionRecord record){
        return template.remove(record);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, TronTransactionRecord.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, TronTransactionRecord.class);
    }
    public Flux<TronTransactionRecord> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, TronTransactionRecord.class, TronTransactionRecord.class);
    }

}
