package com.quickmmo.character.utils;

import com.quickmmo.character.model.Character;

public class CharacterUtils {

    public static Character newCharacter(String name) {
        return new Character(name, 0);
    }
}
