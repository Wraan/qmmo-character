package com.quickmmo.character.web.handler;

import com.quickmmo.character.service.CharacterService;
import com.quickmmo.character.web.dto.CreateCharacterDto;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class CharacterHandler {

    private final String CREATE_CHARACTER_LOCATION_FORMAT = "http://localhost:8080/character/%s";

    private final CharacterService characterService;

    public CharacterHandler(CharacterService characterService) {
        this.characterService = characterService;
    }

    @NonNull
    public Mono<ServerResponse> getCharacter(ServerRequest request) {
        final String characterName = request.pathVariable("name");

        return characterService.getCharacter(characterName)
                .flatMap(character -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(character)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @NonNull
    public Mono<ServerResponse> createCharacter(ServerRequest request) {
        final Mono<CreateCharacterDto> requestBody = request.bodyToMono(CreateCharacterDto.class);

        return requestBody
                .flatMap(dto -> characterService.createCharacter(dto)
                        .thenReturn(dto.getName()))
                .map(name -> String.format(CREATE_CHARACTER_LOCATION_FORMAT, name))
                .flatMap(name -> ServerResponse.created(URI.create(name)).build());
    }
}
