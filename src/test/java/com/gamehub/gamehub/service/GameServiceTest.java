package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.DTO.GameResponse;
import com.gamehub.gamehub.mode1.Game;
import com.gamehub.gamehub.repository.GameRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class GameServiceTest {

    private final GameRepository gameRepository = Mockito.mock(GameRepository.class);
    private final GameService gameService = new GameService(gameRepository);

    @Test
    void getCatalogReturnsGlobalGamesAsDtos() {
        Game minecraft = game(1L, "Minecraft", "https://example.com/minecraft.jpg");
        when(gameRepository.findAll()).thenReturn(List.of(minecraft));

        List<GameResponse> response = gameService.getCatalog();

        assertEquals(1, response.size());
        assertEquals(1L, response.getFirst().id());
        assertEquals("Minecraft", response.getFirst().name());
        assertEquals("https://example.com/minecraft.jpg", response.getFirst().imageUrl());
    }

    @Test
    void searchByNameTrimsQueryAndDelegatesToRepository() {
        Game minecraft = game(1L, "Minecraft", "https://example.com/minecraft.jpg");
        when(gameRepository.findByNameContainingIgnoreCase("mine"))
                .thenReturn(List.of(minecraft));

        List<GameResponse> response = gameService.searchByName(" mine ");

        assertEquals(1, response.size());
        assertEquals("Minecraft", response.getFirst().name());
        verify(gameRepository).findByNameContainingIgnoreCase("mine");
    }

    @Test
    void searchByNameReturnsCatalogWhenQueryIsBlank() {
        Game minecraft = game(1L, "Minecraft", "https://example.com/minecraft.jpg");
        when(gameRepository.findAll()).thenReturn(List.of(minecraft));

        List<GameResponse> response = gameService.searchByName(" ");

        assertEquals(1, response.size());
        assertEquals("Minecraft", response.getFirst().name());
        verify(gameRepository).findAll();
    }

    private Game game(Long id, String name, String imageUrl) {
        Game game = new Game();
        ReflectionTestUtils.setField(game, "id", id);
        ReflectionTestUtils.setField(game, "name", name);
        ReflectionTestUtils.setField(game, "imageUrl", imageUrl);
        return game;
    }
}
