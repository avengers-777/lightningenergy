package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.TronAccount;
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
public class TronAccountMapper {

    private final ReactiveMongoTemplate template;
    public Mono<TronAccount> insert(TronAccount account){
        return template.insert(account);
    };
    public Flux<TronAccount> insertAll(List<TronAccount> accounts) {
        return template.insertAll(accounts);
    }
    public Flux<TronAccount> find(Query query){
        return template.find(query, TronAccount.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, TronAccount.class);
    }

    public Mono<TronAccount> save(TronAccount account){
        return template.save(account);
    }
    public Mono<TronAccount> findById(String id){
        return template.findById(id, TronAccount.class);
    }
    public Flux<TronAccount> findAll(){
        return template.findAll(TronAccount.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, TronAccount.class);
    }
    public Mono<TronAccount> findOne(Query query){
        return template.findOne(query, TronAccount.class);
    }
    public Mono<DeleteResult> removeObject(TronAccount account){
        return template.remove(account);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, TronAccount.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, TronAccount.class);
    }
    public Flux<TronAccount> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, TronAccount.class, TronAccount.class);
    }

}
