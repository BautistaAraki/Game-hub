package com.gamehub.gamehub.dto;

import com.gamehub.gamehub.model.Platform;

public record UserExternalAccountResponse(
        Long id,
        Long userId,
        Platform platform,
        String externalUserId
) {
}
