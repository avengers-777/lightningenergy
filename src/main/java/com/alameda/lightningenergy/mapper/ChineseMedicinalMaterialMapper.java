package com.alameda.lightningenergy.mapper;


import com.alameda.lightningenergy.entity.data.ChineseMedicinalMaterial;
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
public class ChineseMedicinalMaterialMapper {

    private final ReactiveMongoTemplate template;
    public Mono<ChineseMedicinalMaterial> insert(ChineseMedicinalMaterial chineseMedicinalMaterial){
        return template.insert(chineseMedicinalMaterial);
    };
    public Flux<ChineseMedicinalMaterial> insertAll(List<ChineseMedicinalMaterial> chineseMedicinalMaterialList) {
        return template.insertAll(chineseMedicinalMaterialList);
    }
    public Flux<ChineseMedicinalMaterial> find(Query query){
        return template.find(query, ChineseMedicinalMaterial.class);
    };
    public Mono<Long> count(Query query){
        return template.count(query, ChineseMedicinalMaterial.class);
    }

    public Mono<ChineseMedicinalMaterial> save(ChineseMedicinalMaterial chineseMedicinalMaterial){
        return template.save(chineseMedicinalMaterial);
    }
    public Mono<ChineseMedicinalMaterial> findById(String id){
        return template.findById(id, ChineseMedicinalMaterial.class);
    }
    public Flux<ChineseMedicinalMaterial> findAll(){
        return template.findAll(ChineseMedicinalMaterial.class);
    }
    public Mono<DeleteResult> remove(Query query){
        return template.remove(query, ChineseMedicinalMaterial.class);
    }
    public Mono<ChineseMedicinalMaterial> findOne(Query query){
        return template.findOne(query, ChineseMedicinalMaterial.class);
    }
    public Mono<DeleteResult> removeObject(ChineseMedicinalMaterial chineseMedicinalMaterial){
        return template.remove(chineseMedicinalMaterial);
    }

    public Mono<UpdateResult> updateMulti(Query query, Update update){
        return template.updateMulti(query,update, ChineseMedicinalMaterial.class);
    };
    public Mono<UpdateResult> updateFirst(Query query, Update update){
        return template.updateFirst(query, update, ChineseMedicinalMaterial.class);
    }
    public Flux<ChineseMedicinalMaterial> findRandomSampleWithQuery(Query query, int sampleSize) {
        AggregationOperation matchOperation = context -> new org.bson.Document("$match", context.getMappedObject(query.getQueryObject()));
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(sampleSize)
        );
        return template.aggregate(aggregation, ChineseMedicinalMaterial.class, ChineseMedicinalMaterial.class);
    }

}
