package com.quickmmo.character.cache.impl;

import com.quickmmo.character.cache.CacheService;
import com.quickmmo.character.model.Character;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Service
public class RedisCacheService implements CacheService {

    private final ReactiveRedisTemplate<String, Character> redisTemplate;

    public RedisCacheService(ReactiveRedisTemplate<String, Character> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Character> get(String key) {
        return redisTemplate.opsForValue()
                .get(key)
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.printf("Cache key missed: %s%n", key);
                    return Mono.empty();
                }));
    }

    @Override
    public Mono<Void> set(String key, Character value) {
        return redisTemplate.opsForValue()
                .set(key, value, Duration.of(5, ChronoUnit.MINUTES))
                .then();
    }
}
