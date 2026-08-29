package com.gamehub.gamehub.controller;
import com.gamehub.gamehub.service.UserGameService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gamehub.gamehub.mode1.UserGame;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gamehub.gamehub.DTO.UserGameResponse;
import com.gamehub.gamehub.mode1.GameStatus;
import java.util.List;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
@RestController
@RequestMapping("/api/library")
public class UserGameController {
    private final UserGameService userGameService;
    public UserGameController(UserGameService userGameService){
        this.userGameService = userGameService;
    }
    @GetMapping("/users/{userId}")
public List<UserGameResponse> getLibrary(
        @PathVariable("userId") Long userId
) {
    return userGameService.getLibrary(userId);
}
    @PostMapping
    public UserGameResponse addGame(
        @RequestParam Long userId,
        @RequestParam Long gameId
    ){
        return userGameService.addGame(userId, gameId);
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
