package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.Admin;
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
public class AdminMapper {

    private final ReactiveMongoTemplate template;
    public Mono<Admin> insert(Admin admin){
        return template.insert(admin);
    };
    public Flux<Admin> insertAll(List<Admin> admins) {
        return template.insertAll(admins);
    }
    public Flux<Admin> find(Query query){
        return template.find(query, Admin.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, Admin.class);
    }

    public Mono<Admin> save(Admin admin){
        return template.save(admin);
    }
    public Mono<Admin> findById(String id){
        return template.findById(id, Admin.class);
    }
    public Flux<Admin> findAll(){
        return template.findAll(Admin.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, Admin.class);
    }
    public Mono<Admin> findOne(Query query){
        return template.findOne(query, Admin.class);
    }
    public Mono<DeleteResult> removeObject(Admin admin){
        return template.remove(admin);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, Admin.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, Admin.class);
    }
    public Flux<Admin> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, Admin.class, Admin.class);
    }

}
