package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.LinkSteamAccountRequest;
import com.gamehub.gamehub.dto.UserExternalAccountResponse;
import com.gamehub.gamehub.service.UserExternalAccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/external-accounts")
public class UserExternalAccountController {

    private final UserExternalAccountService userExternalAccountService;

    public UserExternalAccountController(
            UserExternalAccountService userExternalAccountService
    ) {
        this.userExternalAccountService = userExternalAccountService;
    }

    @PostMapping("/steam")
    public UserExternalAccountResponse linkSteamAccount(
            @PathVariable Long userId,
            @Valid @RequestBody LinkSteamAccountRequest request
    ) {
        return userExternalAccountService.linkSteamAccount(userId, request);
    }
}
