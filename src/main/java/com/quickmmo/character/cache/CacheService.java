package com.quickmmo.character.cache;

import com.quickmmo.character.model.Character;
import reactor.core.publisher.Mono;

public interface CacheService {

    Mono<Character> get(String key);

    Mono<Void> set(String key, Character value);
}
