package com.gamehub.gamehub.dto;

import jakarta.validation.constraints.NotNull;

public record AddGameToLibraryRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long userId,

        @NotNull(message = "El juego es obligatorio")
        Long gameId
) {
}
