package com.gamehub.gamehub.dto;

public record SteamGameResponse(
        Long appId,
        String name,
        Integer playtimeForeverMinutes,
        String iconUrl
) {
}
