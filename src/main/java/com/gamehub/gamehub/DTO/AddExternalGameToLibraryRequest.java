package com.gamehub.gamehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddExternalGameToLibraryRequest(
        @NotNull(message = "El userId es obligatorio")
        Long userId,

        @NotBlank(message = "La fuente externa es obligatoria")
        String source,

        @NotBlank(message = "El ID externo es obligatorio")
        String externalId,

        @NotBlank(message = "El titulo del juego es obligatorio")
        String title,

        String imageUrl,

        String description,

        String releaseDate,

        List<String> platforms
) {
}
