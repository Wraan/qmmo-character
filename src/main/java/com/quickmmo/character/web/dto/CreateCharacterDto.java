package com.quickmmo.character.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateCharacterDto {
    private String name;

    @JsonCreator
    public CreateCharacterDto(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CreateCharacterDto{" +
                "name='" + name + '\'' +
                '}';
    }
}
