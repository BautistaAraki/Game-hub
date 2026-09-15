package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.dto.LibraryStatsResponse;
import com.gamehub.gamehub.model.Game;
import com.gamehub.gamehub.model.GameStatus;
import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.model.UserGame;
import com.gamehub.gamehub.repository.UserGameRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LibraryStatsServiceTest {

    private final UserGameRepository userGameRepository = Mockito.mock(UserGameRepository.class);
    private final LibraryStatsService libraryStatsService =
            new LibraryStatsService(userGameRepository);

    @Test
    void getStatsCalculatesLibraryCounters() {
        User user = new User("bauti", "bauti@example.com", "hash");
        UserGame completedFavorite = userGame(user, "Minecraft", GameStatus.COMPLETED, 125, true);
        UserGame backlog = userGame(user, "Hades", GameStatus.BACKLOG, null, false);
        UserGame playing = userGame(user, "Celeste", GameStatus.PLAYING, 60, false);

        when(userGameRepository.findByUser_Id(1L))
                .thenReturn(List.of(completedFavorite, backlog, playing));

        LibraryStatsResponse response = libraryStatsService.getStats(1L);

        assertEquals(1L, response.userId());
        assertEquals(3, response.totalGames());
        assertEquals(1, response.completedGames());
        assertEquals(1, response.backlogGames());
        assertEquals(1, response.playingGames());
        assertEquals(0, response.onHoldGames());
        assertEquals(0, response.droppedGames());
        assertEquals(1, response.favoriteGames());
        assertEquals(185, response.totalPlaytimeMinutes());
        assertEquals(3, response.totalPlaytimeHours());
    }

    @Test
    void getStatsReturnsZeroCountersForEmptyLibrary() {
        when(userGameRepository.findByUser_Id(1L)).thenReturn(List.of());

        LibraryStatsResponse response = libraryStatsService.getStats(1L);

        assertEquals(0, response.totalGames());
        assertEquals(0, response.completedGames());
        assertEquals(0, response.totalPlaytimeMinutes());
        assertEquals(0, response.totalPlaytimeHours());
    }

    private UserGame userGame(
            User user,
            String gameName,
            GameStatus status,
            Integer playtimeMinutes,
            boolean favorite
    ) {
        UserGame userGame = new UserGame(
                user,
                new Game(gameName, "https://example.com/" + gameName + ".jpg")
        );
        userGame.changeStatus(status);
        userGame.setplaytimeMinutes(playtimeMinutes);

        if (favorite) {
            userGame.markAsFavorite();
        }

        return userGame;
    }
}
