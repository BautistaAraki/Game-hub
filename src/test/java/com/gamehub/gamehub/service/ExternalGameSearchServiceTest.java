package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.dto.AddExternalGameToLibraryRequest;
import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import com.gamehub.gamehub.dto.UserGameResponse;
import com.gamehub.gamehub.integration.externalgames.ExternalGameClient;
import com.gamehub.gamehub.model.Game;
import com.gamehub.gamehub.model.Platform;
import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.model.UserGame;
import com.gamehub.gamehub.repository.ExternalGameIdRepository;
import com.gamehub.gamehub.repository.GameRepository;
import com.gamehub.gamehub.repository.UserGameRepository;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class ExternalGameSearchServiceTest {

    private final ExternalGameClient externalGameClient = Mockito.mock(ExternalGameClient.class);
    private final ExternalGameIdRepository externalGameIdRepository =
            Mockito.mock(ExternalGameIdRepository.class);
    private final GameRepository gameRepository = Mockito.mock(GameRepository.class);
    private final UserGameRepository userGameRepository = Mockito.mock(UserGameRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private final ExternalGameSearchService externalGameSearchService =
            new ExternalGameSearchService(
                    externalGameClient,
                    externalGameIdRepository,
                    gameRepository,
                    userGameRepository,
                    userRepository
            );

    @Test
    void searchGamesTrimsQueryAndDelegatesToClient() {
        ExternalGameSearchResponse minecraft = new ExternalGameSearchResponse(
                "IGDB",
                "1020",
                "Minecraft",
                "Blocks and survival",
                "https://example.com/minecraft.jpg",
                "Nov 18, 2011",
                List.of("Windows", "macOS")
        );
        when(externalGameClient.searchGames("minecraft")).thenReturn(List.of(minecraft));

        List<ExternalGameSearchResponse> response =
                externalGameSearchService.searchGames(" minecraft ");

        assertEquals(1, response.size());
        assertEquals("Minecraft", response.getFirst().title());
        verify(externalGameClient).searchGames("minecraft");
    }

    @Test
    void addToLibraryCreatesGlobalGameFromExternalResult() {
        User user = new User("Bautista", "bauti@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(externalGameIdRepository.findByPlatformAndExternalId(
                Platform.IGDB,
                "1020"
        )).thenReturn(Optional.empty());
        when(gameRepository.findByNameIgnoreCase("Minecraft")).thenReturn(Optional.empty());
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            ReflectionTestUtils.setField(game, "id", 10L);
            return game;
        });
        when(userGameRepository.existsByUser_IdAndGame_Id(1L, 10L)).thenReturn(false);
        when(userGameRepository.save(any(UserGame.class))).thenAnswer(invocation -> {
            UserGame userGame = invocation.getArgument(0);
            ReflectionTestUtils.setField(userGame, "id", 20L);
            return userGame;
        });

        AddExternalGameToLibraryRequest request = new AddExternalGameToLibraryRequest(
                1L,
                "IGDB",
                "1020",
                "Minecraft",
                "https://example.com/minecraft.jpg",
                "Blocks and survival",
                "Nov 18, 2011",
                List.of("Windows", "macOS")
        );

        UserGameResponse response = externalGameSearchService.addToLibrary(request);

        assertEquals(20L, response.id());
        assertEquals(1L, response.userId());
        assertEquals(10L, response.gameId());
        verify(gameRepository).save(any(Game.class));
        verify(userGameRepository).save(any(UserGame.class));
    }
}
