package com.gamehub.gamehub.dto;

import jakarta.validation.constraints.NotBlank;

public record LinkSteamAccountRequest(
        @NotBlank(message = "El Steam ID es obligatorio")
        String steamId
) {
}
