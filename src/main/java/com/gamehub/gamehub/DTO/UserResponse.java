package com.gamehub.gamehub.dto;

public record UserResponse(
        Long id,
        String username,
        String email
) {
}
