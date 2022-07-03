package com.quickmmo.character.web.handler;

import com.quickmmo.character.exceptions.character.NameConflictException;
import com.quickmmo.character.model.Character;
import com.quickmmo.character.service.CharacterService;
import com.quickmmo.character.utils.CharacterUtils;
import com.quickmmo.character.web.dto.CreateCharacterDto;
import com.quickmmo.character.web.router.CharacterRouter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {CharacterRouter.class, CharacterHandler.class})
@WebFluxTest
@ExtendWith(SpringExtension.class)
public class CharacterHandlerTest {

    private static final String CHARACTER_NAME = "Character";
    private static final String CHARACTER_PATH = "/character";
    private static final String CHARACTER_NAME_PATH = "/character/{name}";
    @Autowired
    private ApplicationContext context;
    @MockBean
    private CharacterService characterService;
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    /**
     * GET /character/{name} tests
     */
    @Test
    public void whenGetCharacter_thenReturnCharacter() {
        // given
        final Character expectedCharacter = CharacterUtils.newCharacter(CHARACTER_NAME);
        when(characterService.getCharacter(CHARACTER_NAME)).thenReturn(Mono.just(expectedCharacter));
        // when
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(CHARACTER_NAME_PATH)
                        .build(CHARACTER_NAME))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Character.class)
                .value(characterResponse -> {
                            Assertions.assertThat(characterResponse).isEqualTo(expectedCharacter);
                        }
                );
    }

    @Test
    public void whenGetCharacter_thenReturnNotFound() {
        // given
        when(characterService.getCharacter(CHARACTER_NAME)).thenReturn(Mono.empty());
        // when
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(CHARACTER_NAME_PATH)
                        .build(CHARACTER_NAME))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * POST /character tests
     */
    @Test
    public void whenCreateCharacter_thenReturnCreated() {
        // given
        final CreateCharacterDto characterDto = new CreateCharacterDto(CHARACTER_NAME);
        when(characterService.createCharacter(characterDto)).thenReturn(Mono.empty());
        // when
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(CHARACTER_PATH)
                        .build())
                .body(BodyInserters.fromValue(characterDto))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(String.format("http://localhost:8080/character/%s", CHARACTER_NAME));
    }

    @Test
    public void whenCreateCharacter_thenReturnConflict() {
        // given
        final CreateCharacterDto characterDto = new CreateCharacterDto(CHARACTER_NAME);
        when(characterService.createCharacter(characterDto)).thenReturn(Mono.error(new NameConflictException(CHARACTER_NAME)));
        // when
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(CHARACTER_PATH)
                        .build())
                .body(BodyInserters.fromValue(characterDto))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

}
