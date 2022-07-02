package com.quickmmo.character.exceptions.character;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NameConflictException extends ResponseStatusException {

    public NameConflictException(String name) {
        super(HttpStatus.CONFLICT, String.format("Character with name %s already exists.", name));
    }
}
