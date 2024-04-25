package com.alameda.lightningenergy.config.redis;
import com.alameda.lightningenergy.entity.data.Admin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RedisConfig {


//    @Bean
//    @Primary
//    public ReactiveRedisConnectionFactory connectionFactory() {
//        return new LettuceConnectionFactory(Objects.requireNonNull(environment.getProperty("spring.data.redis.host")), Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.data.redis.port"))));
//    }

//    @Bean
//    public ReactiveRedisConnectionFactory lettuceConnectionFactory() {
//
//        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//            .useSsl().and()
//            .commandTimeout(Duration.ofSeconds(2))
//            .shutdownTimeout(Duration.ZERO)
//            .build();
//
//        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379), clientConfig);
//    }

    //    @Bean
//    public ReactiveRedisTemplate<String, Post> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
//        return new ReactiveRedisTemplate<String, Post>(
//                factory,
//                RedisSerializationContext.fromSerializer(new Jackson2JsonRedisSerializer(Post.class))
//        );
//    }
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());

    }
    @Bean
    public ReactiveRedisTemplate<String, Long> reactiveLongRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer<Long> valueSerializer =
                new Jackson2JsonRedisSerializer<>(Long.class);
        // 定义RedisSerializationContext
        RedisSerializationContext.RedisSerializationContextBuilder<String, Long> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);

        RedisSerializationContext<String, Long> context = builder.value(valueSerializer).build();

        // 创建并返回ReactiveRedisTemplate
        return new ReactiveRedisTemplate<>(factory, context);
    }
    @Bean
    public ReactiveRedisTemplate<String, Admin> reactiveAdminRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer<Admin> valueSerializer =
                new Jackson2JsonRedisSerializer<>(Admin.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Admin> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);

        RedisSerializationContext<String, Admin> context =
                builder.value(valueSerializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}