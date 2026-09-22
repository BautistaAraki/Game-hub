package com.gamehub.gamehub.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
