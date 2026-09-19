package com.gamehub.gamehub.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gamehub.gamehub.dto.IgdbGameResponse;
import com.gamehub.gamehub.exception.GlobalExceptionHandler;
import com.gamehub.gamehub.exception.IgdbIntegrationException;
import com.gamehub.gamehub.integration.igdb.IgdbClient;
import com.gamehub.gamehub.service.IgdbService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class IgdbControllerTest {
    private final IgdbClient client = mock(IgdbClient.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new IgdbController(new IgdbService(client)))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void returnsSearchContract() throws Exception {
        when(client.searchGames("game")).thenReturn(List.of(new IgdbGameResponse(1L, "Game", null, null, null)));
        mvc.perform(get("/api/igdb/games/search").param("query", " game "))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].igdbId").value(1))
                .andExpect(jsonPath("$[0].name").value("Game"));
    }

    @Test
    void rejectsInvalidSearchWithoutContactingProvider() throws Exception {
        for (String query : List.of(" ", "x".repeat(201), "a\nb")) {
            mvc.perform(get("/api/igdb/games/search").param("query", query)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(client);
    }

    @Test
    void reportsProviderFailureAsStructured502() throws Exception {
        when(client.searchGames("game")).thenThrow(new IgdbIntegrationException("IGDB no esta configurado en el servidor"));
        mvc.perform(get("/api/igdb/games/search").param("query", "game"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("IGDB no esta configurado en el servidor"))
                .andExpect(jsonPath("$.path").value("/api/igdb/games/search"));
    }
}
