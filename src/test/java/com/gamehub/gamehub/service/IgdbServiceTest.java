package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.dto.IgdbGameResponse;
import com.gamehub.gamehub.integration.igdb.IgdbClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IgdbServiceTest {

    private final IgdbClient igdbClient = Mockito.mock(IgdbClient.class);
    private final IgdbService igdbService = new IgdbService(igdbClient);

    @Test
    void searchGamesDelegatesToIgdbClient() {
        List<IgdbGameResponse> games = List.of(
                new IgdbGameResponse(
                        3498L,
                        "Grand Theft Auto V",
                        "https://example.com/gta.jpg",
                        "2013-09-17",
                        4.47
                )
        );

        when(igdbClient.searchGames("gta")).thenReturn(games);

        List<IgdbGameResponse> response = igdbService.searchGames(" gta ");

        assertEquals(games, response);
        verify(igdbClient).searchGames("gta");
    }

    @Test
    void searchGamesRejectsBlankQuery() {
        assertThrows(
                IllegalArgumentException.class,
                () -> igdbService.searchGames(" ")
        );
    }
}
