package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.LinkSteamAccountRequest;
import com.gamehub.gamehub.dto.UserExternalAccountResponse;
import com.gamehub.gamehub.security.GameHubPrincipal;
import com.gamehub.gamehub.service.UserExternalAccountService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserExternalAccountController {

    private final UserExternalAccountService userExternalAccountService;

    public UserExternalAccountController(
            UserExternalAccountService userExternalAccountService
    ) {
        this.userExternalAccountService = userExternalAccountService;
    }

    @PostMapping("/users/{userId}/external-accounts/steam")
    public UserExternalAccountResponse linkSteamAccount(
            @PathVariable Long userId,
            @Valid @RequestBody LinkSteamAccountRequest request,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        if (!principal.id().equals(userId)) {
            throw new AccessDeniedException("No tenes permiso para vincular cuentas de otro usuario");
        }

        return userExternalAccountService.linkSteamAccount(userId, request);
    }

    @PostMapping("/me/external-accounts/steam")
    public UserExternalAccountResponse linkMySteamAccount(
            @Valid @RequestBody LinkSteamAccountRequest request,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return userExternalAccountService.linkSteamAccount(principal.id(), request);
    }
}
