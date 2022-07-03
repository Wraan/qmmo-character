package com.quickmmo.character.utils;

import com.quickmmo.character.model.Character;
import com.quickmmo.character.web.dto.CreateCharacterDto;

public class CharacterUtils {

    public static Character newCharacter(String name) {
        return new Character(name, 0);
    }

    public static Character newCharacter(CreateCharacterDto dto) {
        return new Character(dto.getName(), 0);
    }
}
