package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.AddGameToLibraryRequest;
import com.gamehub.gamehub.dto.UserGameResponse;
import com.gamehub.gamehub.model.GameStatus;
import com.gamehub.gamehub.security.GameHubPrincipal;
import com.gamehub.gamehub.service.UserGameService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class UserGameController {

    private final UserGameService userGameService;

    public UserGameController(UserGameService userGameService) {
        this.userGameService = userGameService;
    }

    @GetMapping("/users/{userId}")
    public List<UserGameResponse> getLibrary(
            @PathVariable Long userId,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        assertSameUser(userId, principal);
        return userGameService.getLibrary(userId);
    }

    @GetMapping("/me")
    public List<UserGameResponse> getMyLibrary(
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return userGameService.getLibrary(principal.id());
    }

    @PostMapping
    public UserGameResponse addGame(
            @Valid @RequestBody AddGameToLibraryRequest request,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return userGameService.addGame(principal.id(), request.gameId());
    }

    @PatchMapping("/{userGameId}/rating")
    public UserGameResponse updateRating(
            @PathVariable Long userGameId,
            @RequestParam Integer rating,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return userGameService.updateRating(principal.id(), userGameId, rating);
    }

    @PatchMapping("/{userGameId}/status")
    public UserGameResponse updateStatus(
            @PathVariable Long userGameId,
            @RequestParam GameStatus status,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return userGameService.updateStatus(principal.id(), userGameId, status);
    }

    @PatchMapping("/{userGameId}/favorite")
    public UserGameResponse addToFavorites(
            @PathVariable Long userGameId,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return userGameService.addToFavorites(principal.id(), userGameId);
    }

    @PatchMapping("/{userGameId}/unfavorite")
    public UserGameResponse removeFromFavorites(
            @PathVariable Long userGameId,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return userGameService.removeFromFavorites(principal.id(), userGameId);
    }

    @DeleteMapping("/{userGameId}")
    public void removeGame(
            @PathVariable Long userGameId,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        userGameService.removeGame(principal.id(), userGameId);
    }

    private void assertSameUser(Long userId, GameHubPrincipal principal) {
        if (!principal.id().equals(userId)) {
            throw new AccessDeniedException("No tenes permiso para acceder a este usuario");
        }
    }
}
