package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.TransferRecord;
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
public class TransferRecordMapper {

    private final ReactiveMongoTemplate template;
    public Mono<TransferRecord> insert(TransferRecord transferRecord){
        return template.insert(transferRecord);
    };
    public Flux<TransferRecord> insertAll(List<TransferRecord> records) {
        return template.insertAll(records);
    }
    public Flux<TransferRecord> find(Query query){
        return template.find(query, TransferRecord.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, TransferRecord.class);
    }

    public Mono<TransferRecord> save(TransferRecord transferRecord){
        return template.save(transferRecord);
    }
    public Mono<TransferRecord> findById(String id){
        return template.findById(id, TransferRecord.class);
    }
    public Flux<TransferRecord> findAll(){
        return template.findAll(TransferRecord.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, TransferRecord.class);
    }
    public Mono<TransferRecord> findOne(Query query){
        return template.findOne(query, TransferRecord.class);
    }
    public Mono<DeleteResult> removeObject(TransferRecord transferRecord){
        return template.remove(transferRecord);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, TransferRecord.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, TransferRecord.class);
    }
    public Flux<TransferRecord> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, TransferRecord.class, TransferRecord.class);
    }

}
