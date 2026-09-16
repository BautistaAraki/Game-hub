package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.dto.RawgGameResponse;
import com.gamehub.gamehub.integration.rawg.RawgClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RawgServiceTest {

    private final RawgClient rawgClient = Mockito.mock(RawgClient.class);
    private final RawgService rawgService = new RawgService(rawgClient);

    @Test
    void searchGamesDelegatesToRawgClient() {
        List<RawgGameResponse> games = List.of(
                new RawgGameResponse(
                        3498L,
                        "Grand Theft Auto V",
                        "https://example.com/gta.jpg",
                        "2013-09-17",
                        4.47
                )
        );

        when(rawgClient.searchGames("gta")).thenReturn(games);

        List<RawgGameResponse> response = rawgService.searchGames(" gta ");

        assertEquals(games, response);
        verify(rawgClient).searchGames("gta");
    }

    @Test
    void searchGamesRejectsBlankQuery() {
        assertThrows(
                IllegalArgumentException.class,
                () -> rawgService.searchGames(" ")
        );
    }
}
