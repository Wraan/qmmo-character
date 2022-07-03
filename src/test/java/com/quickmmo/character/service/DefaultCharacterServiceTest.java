package com.quickmmo.character.service;

import com.quickmmo.character.cache.CacheService;
import com.quickmmo.character.exceptions.character.NameConflictException;
import com.quickmmo.character.model.Character;
import com.quickmmo.character.repository.CharacterRepository;
import com.quickmmo.character.utils.CharacterUtils;
import com.quickmmo.character.web.dto.CreateCharacterDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.quickmmo.character.cache.CacheKeys.nameToCharacterCacheKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultCharacterServiceTest {

    private static final String CHARACTER_NAME = "Character";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.21");
    @MockBean
    private CacheService cacheService;
    @Autowired
    @SpyBean
    private CharacterRepository characterRepository;
    @Autowired
    private CharacterService characterService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @AfterEach
    void cleanUp() {
        this.characterRepository.deleteAll().block();
        reset(characterRepository);
    }

    /**
     * getCharacter
     */

    @Test
    public void whenGetCharacter_thenReturnCharacter() {
        // given
        final Character expectedCharacter = CharacterUtils.newCharacter(CHARACTER_NAME);
        when(cacheService.get(nameToCharacterCacheKey(CHARACTER_NAME))).thenReturn(Mono.empty());
        when(cacheService.set(nameToCharacterCacheKey(CHARACTER_NAME), expectedCharacter)).thenReturn(Mono.empty());
        characterRepository.save(expectedCharacter).block();

        // when
        final Character foundCharacter = characterService.getCharacter(CHARACTER_NAME).block();

        //then
        assertEquals(expectedCharacter, foundCharacter);
        verify(cacheService, times(1)).get(any());
        verify(characterRepository, times(1)).findByName(any());
        verify(cacheService, times(1)).set(any(), any());
    }

    @Test
    public void whenGetCharacter_thenReturnCharacterFromCache() {
        // given
        final Character expectedCharacter = CharacterUtils.newCharacter(CHARACTER_NAME);
        when(cacheService.get(any())).thenReturn(Mono.just(expectedCharacter));

        // when
        final Character foundCharacter = characterService.getCharacter(CHARACTER_NAME).block();

        //then
        assertEquals(expectedCharacter, foundCharacter);
        verify(cacheService, times(1)).get(any());
        verify(characterRepository, times(0)).findByName(any());
        verify(cacheService, times(0)).set(any(), any());
    }

    @Test
    public void whenGetCharacter_thenReturnEmpty() {
        // given
        when(cacheService.get(any())).thenReturn(Mono.empty());

        // when
        final Character foundCharacter = characterService.getCharacter(CHARACTER_NAME).block();

        //then
        assertNull(foundCharacter);
        verify(cacheService, times(1)).get(any());
        verify(characterRepository, times(1)).findByName(any());
        verify(cacheService, times(0)).set(any(), any());
    }

    /**
     * createCharacter
     */

    @Test
    public void whenCreateCharacter_thenCreateCharacter() {
        // given
        final CreateCharacterDto dto = new CreateCharacterDto(CHARACTER_NAME);
        final Character expectedCharacter = CharacterUtils.newCharacter(dto);
        when(cacheService.get(nameToCharacterCacheKey(CHARACTER_NAME))).thenReturn(Mono.empty());
        when(cacheService.set(nameToCharacterCacheKey(CHARACTER_NAME), expectedCharacter)).thenReturn(Mono.empty());

        // when
        characterService.createCharacter(dto).block();

        //then
        verify(cacheService, times(1)).get(any());
        verify(characterRepository, times(1)).save(any());
        verify(cacheService, times(1)).set(any(), any());
    }

    @Test
    public void whenCreateCharacter_thenThrowNameConflictException() {
        // given
        final CreateCharacterDto dto = new CreateCharacterDto(CHARACTER_NAME);
        final Character expectedCharacter = CharacterUtils.newCharacter(dto);
        characterRepository.save(expectedCharacter).block();
        when(cacheService.get(nameToCharacterCacheKey(CHARACTER_NAME))).thenReturn(Mono.empty());
        when(cacheService.set(nameToCharacterCacheKey(CHARACTER_NAME), expectedCharacter)).thenReturn(Mono.empty());

        // when
        Mono<Void> mono = characterService.createCharacter(dto).log();

        //then
        StepVerifier.create(mono)
                .expectError(NameConflictException.class)
                .verify();
        verify(cacheService, times(1)).get(any());
        verify(characterRepository, times(1)).save(any());
        verify(cacheService, times(1)).set(any(), any());
    }
}
