package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.AddGameToLibraryRequest;
import com.gamehub.gamehub.dto.UserGameResponse;
import com.gamehub.gamehub.model.GameStatus;
import com.gamehub.gamehub.service.UserGameService;
import jakarta.validation.Valid;
import java.util.List;
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
            @PathVariable Long userId
    ) {
        return userGameService.getLibrary(userId);
    }

    @PostMapping
    public UserGameResponse addGame(
            @Valid @RequestBody AddGameToLibraryRequest request
    ) {
        return userGameService.addGame(request.userId(), request.gameId());
    }

    @PatchMapping("/{userGameId}/rating")
    public UserGameResponse updateRating(
            @PathVariable Long userGameId,
            @RequestParam Integer rating
    ) {
        return userGameService.updateRating(userGameId, rating);
    }

    @PatchMapping("/{userGameId}/status")
    public UserGameResponse updateStatus(
            @PathVariable Long userGameId,
            @RequestParam GameStatus status
    ) {
        return userGameService.updateStatus(userGameId, status);
    }

    @PatchMapping("/{userGameId}/favorite")
    public UserGameResponse addToFavorites(
            @PathVariable Long userGameId
    ) {
        return userGameService.addToFavorites(userGameId);
    }

    @PatchMapping("/{userGameId}/unfavorite")
    public UserGameResponse removeFromFavorites(
            @PathVariable Long userGameId
    ) {
        return userGameService.removeFromFavorites(userGameId);
    }

    @DeleteMapping("/{userGameId}")
    public void removeGame(
            @PathVariable Long userGameId
    ) {
        userGameService.removeGame(userGameId);
    }
}
