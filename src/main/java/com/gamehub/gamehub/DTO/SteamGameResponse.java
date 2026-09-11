package com.gamehub.gamehub.DTO;

public record SteamGameResponse(
        Long appId,
        String name,
        Integer playtimeForeverMinutes,
        String iconUrl
) {
}
