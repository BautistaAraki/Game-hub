package com.gamehub.gamehub.dto;

public record GameResponse(
        Long id,
        String name,
        String imageUrl,
        String description,
        String releaseDate,
        String platforms,
        String externalSource
) {
}
