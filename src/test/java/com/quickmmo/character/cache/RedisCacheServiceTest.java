package com.quickmmo.character.cache;

import com.quickmmo.character.cache.impl.RedisCacheService;
import com.quickmmo.character.model.Character;
import com.quickmmo.character.utils.CharacterUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.quickmmo.character.cache.CacheKeys.nameToCharacterCacheKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisCacheServiceTest {

    private static final String CHARACTER_NAME = "Character";
    @Container
    public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:7.0.2-alpine"))
            .withExposedPorts(6379);
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private ReactiveRedisTemplate<String, Character> redisTemplate;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.url", () -> "redis://" + redis.getHost() + ":" + redis.getFirstMappedPort());
    }

    @AfterEach
    void cleanUp() {
        redisTemplate.expire(nameToCharacterCacheKey(CHARACTER_NAME), Duration.ofMillis(1)).block();
    }

    /**
     * get
     */

    @Test
    public void whenGetKey_thenReturnCharacter() {
        // given
        final Character expectedCharacter = CharacterUtils.newCharacter(CHARACTER_NAME);
        setCharacterInCache(nameToCharacterCacheKey(CHARACTER_NAME), expectedCharacter);

        // when
        final Character foundCharacter = redisCacheService.get(nameToCharacterCacheKey(CHARACTER_NAME)).block();

        //then
        assertEquals(expectedCharacter, foundCharacter);
    }

    @Test
    public void whenGetKey_thenReturnEmpty() {
        // when
        final Character foundCharacter = redisCacheService.get(nameToCharacterCacheKey(CHARACTER_NAME)).block();

        //then
        assertNull(foundCharacter);
    }

    /**
     * set
     */

    @Test
    public void whenSetKey_thenSetKey() {
        // given
        final Character expectedCharacter = CharacterUtils.newCharacter(CHARACTER_NAME);

        // when
        redisCacheService.set(nameToCharacterCacheKey(CHARACTER_NAME), expectedCharacter).block();

        //then
        final Character foundCharacter = redisTemplate.opsForValue().get(nameToCharacterCacheKey(CHARACTER_NAME)).block();
        assertEquals(expectedCharacter, foundCharacter);
    }

    private void setCharacterInCache(String key, Character value) {
        redisTemplate.opsForValue().set(key, value, Duration.of(5, ChronoUnit.MINUTES)).block();
    }
}
