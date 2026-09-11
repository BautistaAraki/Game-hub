package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.exception.GameAlreadyInLibraryException;
import com.gamehub.gamehub.mode1.Game;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.mode1.UserGame;
import com.gamehub.gamehub.repository.GameRepository;
import com.gamehub.gamehub.repository.UserGameRepository;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserGameServiceTest {

    private final UserGameRepository userGameRepository = Mockito.mock(UserGameRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final GameRepository gameRepository = Mockito.mock(GameRepository.class);
    private final UserGameService userGameService = new UserGameService(
            userGameRepository,
            userRepository,
            gameRepository
    );

    @Test
    void updateRatingRejectsInvalidRatingAndDoesNotSave() {
        UserGame userGame = new UserGame(
                new User("bauti", "bauti@example.com", "hash"),
                new Game("Minecraft", "https://example.com/minecraft.jpg")
        );

        when(userGameRepository.findById(2L)).thenReturn(Optional.of(userGame));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userGameService.updateRating(2L, 15)
        );

        assertEquals("El rating debe estar entre 1 y 10", exception.getMessage());
        verify(userGameRepository, never()).save(userGame);
    }

    @Test
    void addGameRejectsDuplicateUserGame() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User("bauti", "bauti@example.com", "hash")));
        when(gameRepository.findById(1L))
                .thenReturn(Optional.of(new Game("Minecraft", "https://example.com/minecraft.jpg")));
        when(userGameRepository.existsByUser_IdAndGame_Id(1L, 1L))
                .thenReturn(true);

        assertThrows(
                GameAlreadyInLibraryException.class,
                () -> userGameService.addGame(1L, 1L)
        );

        verify(userGameRepository, never()).save(Mockito.any(UserGame.class));
    }
}
