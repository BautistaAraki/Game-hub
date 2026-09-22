package com.gamehub.gamehub.dto;

import jakarta.validation.constraints.NotNull;

public record AddGameToLibraryRequest(
        Long userId,

        @NotNull(message = "El juego es obligatorio")
        Long gameId
) {
}
