package com.quickmmo.character.cache;

public class CacheKeys {

    private static final String NAME_TO_CHARACTER_CACHE_KEY = "character:name:%s";

    public static String nameToCharacterCacheKey(String name) {
        return String.format(NAME_TO_CHARACTER_CACHE_KEY, name);
    }
}
