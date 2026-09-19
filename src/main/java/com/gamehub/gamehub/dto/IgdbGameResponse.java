package com.gamehub.gamehub.dto;

public record IgdbGameResponse(
        Long igdbId,
        String name,
        String imageUrl,
        String released,
        Double rating
) {
}
