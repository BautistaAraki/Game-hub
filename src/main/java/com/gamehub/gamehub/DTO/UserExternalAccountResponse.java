package com.gamehub.gamehub.DTO;

import com.gamehub.gamehub.mode1.Platform;

public record UserExternalAccountResponse(
        Long id,
        Long userId,
        Platform platform,
        String externalUserId
) {
}
