package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.dto.SteamGameResponse;
import com.gamehub.gamehub.dto.SteamImportResponse;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
import com.gamehub.gamehub.integration.steam.SteamClient;
import com.gamehub.gamehub.model.ExternalGameId;
import com.gamehub.gamehub.model.Game;
import com.gamehub.gamehub.model.Platform;
import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.model.UserExternalAccount;
import com.gamehub.gamehub.model.UserGame;
import com.gamehub.gamehub.repository.ExternalGameIdRepository;
import com.gamehub.gamehub.repository.GameRepository;
import com.gamehub.gamehub.repository.UserExternalAccountRepository;
import com.gamehub.gamehub.repository.UserGameRepository;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class SteamImportServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserExternalAccountRepository userExternalAccountRepository =
            Mockito.mock(UserExternalAccountRepository.class);
    private final ExternalGameIdRepository externalGameIdRepository =
            Mockito.mock(ExternalGameIdRepository.class);
    private final GameRepository gameRepository = Mockito.mock(GameRepository.class);
    private final UserGameRepository userGameRepository = Mockito.mock(UserGameRepository.class);
    private final SteamClient steamClient = Mockito.mock(SteamClient.class);
    private final SteamImportService steamImportService = new SteamImportService(
            userRepository,
            userExternalAccountRepository,
            externalGameIdRepository,
            gameRepository,
            userGameRepository,
            steamClient
    );

    @Test
    void importLibraryCreatesMissingGameAndLibraryEntry() {
        User user = user(1L);
        UserExternalAccount account = new UserExternalAccount(
                user,
                Platform.STEAM,
                "steam-user-id"
        );
        SteamGameResponse steamGame = new SteamGameResponse(
                10L,
                "Counter-Strike",
                120,
                "icon"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userExternalAccountRepository.findByUser_IdAndPlatform(1L, Platform.STEAM))
                .thenReturn(Optional.of(account));
        when(steamClient.getOwnedGames("steam-user-id")).thenReturn(List.of(steamGame));
        when(externalGameIdRepository.findByPlatformAndExternalId(Platform.STEAM, "10"))
                .thenReturn(Optional.empty());
        when(gameRepository.save(Mockito.any(Game.class)))
                .thenAnswer(invocation -> {
                    Game game = invocation.getArgument(0);
                    ReflectionTestUtils.setField(game, "id", 99L);
                    return game;
                });
        when(userGameRepository.existsByUser_IdAndGame_Id(1L, 99L)).thenReturn(false);

        SteamImportResponse response = steamImportService.importLibrary(1L);

        assertEquals(1, response.steamGamesFound());
        assertEquals(1, response.gamesCreated());
        assertEquals(0, response.gamesMatched());
        assertEquals(1, response.libraryEntriesCreated());
        assertEquals(0, response.libraryEntriesSkipped());
        verify(gameRepository).save(Mockito.any(Game.class));
        verify(userGameRepository).save(Mockito.any(UserGame.class));
    }

    @Test
    void importLibraryReusesExistingGameAndSkipsExistingLibraryEntry() {
        User user = user(1L);
        UserExternalAccount account = new UserExternalAccount(
                user,
                Platform.STEAM,
                "steam-user-id"
        );
        Game existingGame = game(99L, "Counter-Strike", "icon");
        ExternalGameId externalGameId = new ExternalGameId(Platform.STEAM, "10");
        existingGame.addExternalID(externalGameId);
        SteamGameResponse steamGame = new SteamGameResponse(
                10L,
                "Counter-Strike",
                120,
                "icon"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userExternalAccountRepository.findByUser_IdAndPlatform(1L, Platform.STEAM))
                .thenReturn(Optional.of(account));
        when(steamClient.getOwnedGames("steam-user-id")).thenReturn(List.of(steamGame));
        when(externalGameIdRepository.findByPlatformAndExternalId(Platform.STEAM, "10"))
                .thenReturn(Optional.of(externalGameId));
        when(userGameRepository.existsByUser_IdAndGame_Id(1L, 99L)).thenReturn(true);

        SteamImportResponse response = steamImportService.importLibrary(1L);

        assertEquals(1, response.steamGamesFound());
        assertEquals(0, response.gamesCreated());
        assertEquals(1, response.gamesMatched());
        assertEquals(0, response.libraryEntriesCreated());
        assertEquals(1, response.libraryEntriesSkipped());
        verify(gameRepository, never()).save(Mockito.any(Game.class));
        verify(userGameRepository, never()).save(Mockito.any(UserGame.class));
    }

    @Test
    void importLibraryRejectsUserWithoutSteamAccount() {
        User user = user(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userExternalAccountRepository.findByUser_IdAndPlatform(1L, Platform.STEAM))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> steamImportService.importLibrary(1L)
        );

        verify(steamClient, never()).getOwnedGames(Mockito.anyString());
    }

    private User user(Long id) {
        User user = new User("bauti", "bauti@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Game game(Long id, String name, String imageUrl) {
        Game game = new Game(name, imageUrl);
        ReflectionTestUtils.setField(game, "id", id);
        return game;
    }
}
