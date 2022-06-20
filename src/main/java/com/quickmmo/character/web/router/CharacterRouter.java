package com.quickmmo.character.web.router;

import com.quickmmo.character.web.handler.CharacterHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class CharacterRouter {

    @Bean
    public RouterFunction<ServerResponse> characterRoute(CharacterHandler characterHandler) {
        return RouterFunctions
                .route(GET("/character/{name}").and(accept(MediaType.APPLICATION_JSON)), characterHandler::getCharacter)
                .andRoute(POST("/character").and(accept(MediaType.APPLICATION_JSON)), characterHandler::createCharacter);
//                .andRoute(DELETE("/character/{name}").and(accept(MediaType.APPLICATION_JSON)), characterHandler::deleteCharacter);
    }
}
