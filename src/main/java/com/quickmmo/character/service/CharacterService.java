package com.quickmmo.character.service;

import com.quickmmo.character.model.Character;
import com.quickmmo.character.web.dto.CreateCharacterDto;
import reactor.core.publisher.Mono;

public interface CharacterService {

    /**
     * Get existing character
     *
     * @param name - name of a character to get
     * @return reqiested Character
     */
    Mono<Character> getCharacter(final String name);

    /**
     * Creates a new character
     *
     * @param dto - dto class for character creation
     * @return void
     */
    Mono<Void> createCharacter(final CreateCharacterDto dto);

}
