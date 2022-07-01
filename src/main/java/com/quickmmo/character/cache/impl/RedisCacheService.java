package com.quickmmo.character.cache.impl;

import com.quickmmo.character.cache.CacheService;
import com.quickmmo.character.model.Character;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
public class RedisCacheService implements CacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheService.class);

    private final ReactiveRedisTemplate<String, Character> redisTemplate;

    public RedisCacheService(ReactiveRedisTemplate<String, Character> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Character> get(String key) {
        LOGGER.debug("Getting key: {}", key);
        return redisTemplate.opsForValue()
                .get(key)
                .switchIfEmpty(Mono.defer(() -> {
                    LOGGER.debug("Cache key missed: {}", key);
                    return Mono.empty();
                }));
    }

    @Override
    public Mono<Void> set(String key, Character value) {
        LOGGER.debug("Setting key {} with value {}", key, value);
        return redisTemplate.opsForValue()
                .set(key, value, Duration.of(5, ChronoUnit.MINUTES))
                .then();
    }
}
