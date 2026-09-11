package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.DTO.SteamGameResponse;
import com.gamehub.gamehub.integration.steam.SteamClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SteamServiceTest {

    private final SteamClient steamClient = Mockito.mock(SteamClient.class);
    private final SteamService steamService = new SteamService(steamClient);

    @Test
    void getOwnedGamesDelegatesToSteamClient() {
        List<SteamGameResponse> games = List.of(
                new SteamGameResponse(10L, "Counter-Strike", 120, "icon")
        );

        when(steamClient.getOwnedGames("76561198000000000")).thenReturn(games);

        List<SteamGameResponse> response = steamService.getOwnedGames(" 76561198000000000 ");

        assertEquals(games, response);
        verify(steamClient).getOwnedGames("76561198000000000");
    }

    @Test
    void getOwnedGamesRejectsBlankSteamId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> steamService.getOwnedGames(" ")
        );
    }
}
