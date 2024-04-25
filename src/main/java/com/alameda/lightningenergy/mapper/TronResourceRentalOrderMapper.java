package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.TronResourceRentalOrder;
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
public class TronResourceRentalOrderMapper {

    private final ReactiveMongoTemplate template;
    public Mono<TronResourceRentalOrder> insert(TronResourceRentalOrder order){
        return template.insert(order);
    };
    public Flux<TronResourceRentalOrder> insertAll(List<TronResourceRentalOrder> orders) {
        return template.insertAll(orders);
    }
    public Flux<TronResourceRentalOrder> find(Query query){
        return template.find(query, TronResourceRentalOrder.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, TronResourceRentalOrder.class);
    }

    public Mono<TronResourceRentalOrder> save(TronResourceRentalOrder order){
        return template.save(order);
    }
    public Mono<TronResourceRentalOrder> findById(String id){
        return template.findById(id, TronResourceRentalOrder.class);
    }
    public Flux<TronResourceRentalOrder> findAll(){
        return template.findAll(TronResourceRentalOrder.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, TronResourceRentalOrder.class);
    }
    public Mono<TronResourceRentalOrder> findOne(Query query){
        return template.findOne(query, TronResourceRentalOrder.class);
    }
    public Mono<DeleteResult> removeObject(TronResourceRentalOrder order){
        return template.remove(order);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, TronResourceRentalOrder.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, TronResourceRentalOrder.class);
    }
    public Flux<TronResourceRentalOrder> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, TronResourceRentalOrder.class, TronResourceRentalOrder.class);
    }

}
