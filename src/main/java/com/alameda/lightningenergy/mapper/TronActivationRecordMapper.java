package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.TronActivationRecord;
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
public class TronActivationRecordMapper {

    private final ReactiveMongoTemplate template;
    public Mono<TronActivationRecord> insert(TronActivationRecord record){
        return template.insert(record);
    };
    public Flux<TronActivationRecord> insertAll(List<TronActivationRecord> records) {
        return template.insertAll(records);
    }
    public Flux<TronActivationRecord> find(Query query){
        return template.find(query, TronActivationRecord.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, TronActivationRecord.class);
    }

    public Mono<TronActivationRecord> save(TronActivationRecord record){
        return template.save(record);
    }
    public Mono<TronActivationRecord> findById(String id){
        return template.findById(id, TronActivationRecord.class);
    }
    public Flux<TronActivationRecord> findAll(){
        return template.findAll(TronActivationRecord.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, TronActivationRecord.class);
    }
    public Mono<TronActivationRecord> findOne(Query query){
        return template.findOne(query, TronActivationRecord.class);
    }
    public Mono<DeleteResult> removeObject(TronActivationRecord record){
        return template.remove(record);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, TronActivationRecord.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, TronActivationRecord.class);
    }
    public Flux<TronActivationRecord> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, TronActivationRecord.class, TronActivationRecord.class);
    }

}
