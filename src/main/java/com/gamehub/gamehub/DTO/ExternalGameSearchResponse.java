package com.gamehub.gamehub.dto;

public record ExternalGameSearchResponse(
        String source,
        String externalId,
        String title,
        String description,
        String imageUrl,
        String releaseDate
) {
}
