package com.gamehub.gamehub.dto;

public record RawgGameResponse(
        Long rawgId,
        String name,
        String imageUrl,
        String released,
        Double rating
) {
}
