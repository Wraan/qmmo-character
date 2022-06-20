package com.quickmmo.character.repository;

import com.quickmmo.character.model.Character;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CharacterRepository extends ReactiveMongoRepository<Character, String> {

    Mono<Character> findByName(String name);

}
