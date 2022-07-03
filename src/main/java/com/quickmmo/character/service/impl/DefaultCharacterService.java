package com.quickmmo.character.service.impl;

import com.quickmmo.character.cache.CacheService;
import com.quickmmo.character.exceptions.character.NameConflictException;
import com.quickmmo.character.model.Character;
import com.quickmmo.character.repository.CharacterRepository;
import com.quickmmo.character.service.CharacterService;
import com.quickmmo.character.web.dto.CreateCharacterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.quickmmo.character.cache.CacheKeys.nameToCharacterCacheKey;
import static com.quickmmo.character.utils.CharacterUtils.newCharacter;

@Service
public class DefaultCharacterService implements CharacterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCharacterService.class);

    private final CharacterRepository characterRepository;
    private final CacheService cacheService;

    public DefaultCharacterService(CharacterRepository characterRepository,
                                   CacheService cacheService) {
        this.characterRepository = characterRepository;
        this.cacheService = cacheService;
    }

    @Override
    public Mono<Character> getCharacter(String name) {
        LOGGER.debug("Getting character with name {}.", name);
        return cacheService.get(nameToCharacterCacheKey(name))
                .switchIfEmpty(Mono.defer(() -> characterRepository.findByName(name))
                        .flatMap(character -> cacheService.set(nameToCharacterCacheKey(name), character)
                                .thenReturn(character)));

    }

    @Override
    public Mono<Void> createCharacter(final CreateCharacterDto dto) {
        LOGGER.debug("Creating character with name {} - received dto={}", dto.getName(), dto);

        return getCharacter(dto.getName())
                .flatMap(character -> Mono.error(new NameConflictException(dto.getName())))
                .switchIfEmpty(Mono.defer(() -> characterRepository.save(newCharacter(dto)))
                        .flatMap(character -> cacheService.set(nameToCharacterCacheKey(character.getName()), character)))
                .then();
    }
}
