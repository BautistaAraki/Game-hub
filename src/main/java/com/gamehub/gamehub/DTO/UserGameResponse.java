package com.gamehub.gamehub.DTO;
import com.gamehub.gamehub.mode1.GameStatus;
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
