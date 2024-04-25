package com.alameda.lightningenergy.service;

import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.utils.BlockUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ReactiveRedisTemplate<String, Long> reactiveLongRedisTemplate;

    public Mono<Boolean> isAllowedIp(String ip){
        return isAllowed(LimitType.LOCK_IP_ +ip,1L,Duration.ofMinutes(1))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(ErrorType.SYSTEM_BUSY.getException()));
    }
    public Mono<Boolean> isAllowedBlockHeight(Long height){
        return isAllowed(LimitType.LOCK_BLOCK_HEIGHT_ +height.toString(),1L,Duration.ofMinutes(1))
                .filter(Boolean::booleanValue);
    }
    public Mono<Boolean> clearLockedBlockHeight(Long height){
        return resetCounter(LimitType.LOCK_BLOCK_HEIGHT_ +height.toString());
    }
    public Mono<Boolean> clearLockedIp(String ip){
        return resetCounter(LimitType.LOCK_IP_ +ip);
    }
    public Mono<Boolean> batchLockAddress(List<String> addressList) {
        return Flux.fromIterable(addressList)
                .flatMap(address -> isAllowed(LimitType.LOCK_EXTERNAL_ADDRESS_ + address, 1L, Duration.ofMinutes(1)))
                .all(Boolean::booleanValue)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(ErrorType.SYSTEM_BUSY.getException()));
    }
    public Mono<Boolean> batchClearAddress(List<String> addressList) {
        return Flux.fromIterable(addressList)
                .flatMap(address -> resetCounter(LimitType.LOCK_EXTERNAL_ADDRESS_ + address))
                .then(Mono.just(true));
    }
    public Mono<Boolean> lockingRentalOrder(String orderId){
        return isAllowed(LimitType.LOCK_RENTAL_ORDER_ +orderId,1L,Duration.ofMinutes(1))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(ErrorType.SYSTEM_BUSY.getException()));
    }
    public Mono<Boolean> clearLockedRentalOrder(String orderId){
        return resetCounter(LimitType.LOCK_RENTAL_ORDER_ +orderId);
    }
    public Mono<Boolean> lockingTronAccount(String id) {
        return getLockingTronAccount()
                .filter(set -> !set.contains(id))
                .switchIfEmpty(Mono.error(ErrorType.ACCOUNT_LOCKED.getException()))
                .map(set -> {
                    set.add(id);
                    return set;
                })
                .flatMap(set -> saveSet(LimitType.LOCKED_TRON_ACCOUNT,set))
                .thenReturn(true);
    }
    public Mono<Set<String>> getLockingTronAccount(){
        return getSet(LimitType.LOCKED_TRON_ACCOUNT);
    }
    public Mono<Boolean> unlockTronAccount(String id) {
        return getLockingTronAccount()
                .map(set -> {
                    set.remove(id);
                    return set;
                })
                .flatMap(set -> saveSet(LimitType.LOCKED_TRON_ACCOUNT,set))
                .thenReturn(true);
    }
    public Mono<Double> calculateTronCurrentBlockHeightDifference(){
        Mono<Long> blockHeightMono = getLongValue(LimitType.TRON_BLOCK_HEIGHT_RECORD);
        Mono<Long> blockUpdateTime = getLongValue(LimitType.TRON_BLOCK_HEIGHT_UPDATE_TIME);
        Mono<Long> scanBlockHeight = getLongValue(LimitType.TRON_BLOCK_HEIGHT);
        return Mono.zip(blockHeightMono,blockUpdateTime,scanBlockHeight)
                .map(tuples -> BlockUtils.calculateCurrentBlockHeightDifference(tuples.getT1(),tuples.getT2(),tuples.getT3()));
    }
    public Mono<Tuple2<Long, Long>>  getBlockHeightAndUpdateTime(){
        Mono<Long> blockHeightMono = getLongValue(LimitType.TRON_BLOCK_HEIGHT_RECORD);
        Mono<Long> blockUpdateTime = getLongValue(LimitType.TRON_BLOCK_HEIGHT_UPDATE_TIME);
        return Mono.zip(blockHeightMono,blockUpdateTime);
    }
    public Mono<Long> getTronBlockHeight(){
        return getLongValue(LimitType.TRON_BLOCK_HEIGHT);
    }
    public Mono<Boolean> saveTronBlockHeight(long height){
        Mono<Boolean> saveHeightMono = saveLongValue(LimitType.TRON_BLOCK_HEIGHT,height);
        return saveHeightMono
                .filter(t -> t)
                .switchIfEmpty(Mono.error(ErrorType.REDIS_CACHE_EXCEPTION.getException()))
                .hasElement();
    }
    public Mono<Boolean> saveTronBlockHeightRecord(long height,long timestamp){
        Mono<Boolean> saveHeightMono = saveLongValue(LimitType.TRON_BLOCK_HEIGHT_RECORD,height);
        Mono<Boolean> saveUpdateTime = saveLongValue(LimitType.TRON_BLOCK_HEIGHT_UPDATE_TIME,timestamp);
        return Mono.zip(saveHeightMono,saveUpdateTime)
                .filter(t -> t.getT1() && t.getT2())
                .switchIfEmpty(Mono.error(ErrorType.REDIS_CACHE_EXCEPTION.getException()))
                .hasElement();
    }

    private Mono<Long> saveSet(LimitType key, Set<String> set) {
        Mono<Long> deleteMono = Mono.defer(()-> reactiveRedisTemplate.delete(key.toString()));
        return Mono.just(set)
                .filter(s -> !s.isEmpty())
                .flatMap(s -> reactiveRedisTemplate.opsForSet().add(key.toString(), s.toArray(new String[0])))
                .flatMap(l ->  reactiveRedisTemplate.expire(key.toString(), Duration.ofMinutes(1)).thenReturn(l))
                .switchIfEmpty(deleteMono)
                .filter(l -> l != 0)
                .switchIfEmpty(Mono.error(ErrorType.REDIS_LOCK_EXCEPTION.getException()));

    }
    private Mono<Set<String>> getSet(LimitType key) {
        return reactiveRedisTemplate.opsForSet().members(key.toString()).collect(Collectors.toSet());
    }
    private Mono<Boolean> saveLongValue(LimitType key, Long value) {
        // 使用opsForValue()方法来操作简单的值
        return reactiveLongRedisTemplate.opsForValue().set(key.toString(), value);
    }
    private Mono<Long> getLongValue(LimitType key) {
        // 使用opsForValue().get()方法来读取值
        return reactiveLongRedisTemplate.opsForValue().get(key.toString());
    }
    public Mono<Boolean> resetCounter(String key) {
        return reactiveRedisTemplate.delete(key).map(deleted -> deleted != 0);
    }


    private Mono<Boolean> isAllowed(String key, long limit, Duration duration) {
        return reactiveRedisTemplate.opsForValue().increment(key, 0).flatMap(count -> {
            if (count < limit) {
                return reactiveRedisTemplate.opsForValue().increment(key, 1).flatMap(newCount -> {
                    if (newCount == 1) {
                        return reactiveRedisTemplate.expire(key, duration).thenReturn(true);
                    }
                    return Mono.just(true);
                });
            } else {
                return Mono.just(false);
            }
        });
    }
    private Mono<Boolean> decrementCount(String key) {
        return reactiveRedisTemplate.opsForValue().decrement(key, 1)
                .flatMap(newCount -> Mono.just(newCount >= 0))
                .defaultIfEmpty(false);
    }
    private enum LimitType{
        LOCKED_TRON_ACCOUNT,
        TRON_BLOCK_HEIGHT,
        TRON_BLOCK_HEIGHT_RECORD,
        TRON_BLOCK_HEIGHT_UPDATE_TIME,
        LOCK_IP_,
        LOCK_EXTERNAL_ADDRESS_,
        LOCK_RENTAL_ORDER_,
        LOCK_BLOCK_HEIGHT_

    }


}
