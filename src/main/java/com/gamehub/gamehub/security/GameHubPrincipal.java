package com.gamehub.gamehub.security;

public record GameHubPrincipal(
        Long id,
        String username,
        String email
) {
}
