package com.alameda.lightningenergy.config.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories
@RequiredArgsConstructor
public class MongoReactiveApplication {

    @Bean
    public  ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    private final Environment environment;
    @Autowired
    private ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory;

//    @Bean
//    public MongoClient mongoClient() {
//        ConnectionString connString = new ConnectionString(Objects.requireNonNull(environment.getProperty("spring.data.mongodb.uri")));
//        ServerApi serverApi = ServerApi.builder()
//                .version(ServerApiVersion.V1)
//                .build();
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .applyConnectionString(connString)
//                .retryWrites(true)
//                .serverApi(serverApi)
//                .build();
//        return MongoClients.create(settings);
//    }

//    public @Bean ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory() {
//        return new SimpleReactiveMongoDatabaseFactory(MongoClients.create("mongodb://joe:secret@localhost"), "database");
//    }
    @Bean
    public ReactiveMongoTransactionManager reactiveMongoTransactionManager() {
        return new ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory);
    }
//    public @Bean ReactiveMongoTemplate reactiveMongoTemplate() {
//        return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory());
//    }


}