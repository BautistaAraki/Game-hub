package com.gamehub.gamehub.dto;

import com.gamehub.gamehub.model.GameStatus;

public record UserGameResponse(
        Long id,
        Long userId,
        Long gameId,
        Integer rating,
        Integer playtimeMinutes,
        boolean favorite,
        GameStatus status
) {
}
