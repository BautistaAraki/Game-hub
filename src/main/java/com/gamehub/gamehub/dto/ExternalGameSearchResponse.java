package com.gamehub.gamehub.dto;

import java.util.List;

public record ExternalGameSearchResponse(
        String source,
        String externalId,
        String title,
        String description,
        String imageUrl,
        String releaseDate,
        List<String> platforms
) {
}
