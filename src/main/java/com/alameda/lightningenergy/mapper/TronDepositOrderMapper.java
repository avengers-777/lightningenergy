package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.DepositOrder;
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
public class TronDepositOrderMapper {

    private final ReactiveMongoTemplate template;
    public Mono<DepositOrder> insert(DepositOrder order){
        return template.insert(order);
    };
    public Flux<DepositOrder> insertAll(List<DepositOrder> orders) {
        return template.insertAll(orders);
    }
    public Flux<DepositOrder> find(Query query){
        return template.find(query, DepositOrder.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, DepositOrder.class);
    }

    public Mono<DepositOrder> save(DepositOrder order){
        return template.save(order);
    }
    public Mono<DepositOrder> findById(String id){
        return template.findById(id, DepositOrder.class);
    }
    public Flux<DepositOrder> findAll(){
        return template.findAll(DepositOrder.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, DepositOrder.class);
    }
    public Mono<DepositOrder> findOne(Query query){
        return template.findOne(query, DepositOrder.class);
    }
    public Mono<DeleteResult> removeObject(DepositOrder order){
        return template.remove(order);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, DepositOrder.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, DepositOrder.class);
    }
    public Flux<DepositOrder> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, DepositOrder.class, DepositOrder.class);
    }

}
