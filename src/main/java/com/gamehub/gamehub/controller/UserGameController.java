package com.gamehub.gamehub.controller;
import com.gamehub.gamehub.service.UserGameService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gamehub.gamehub.mode1.UserGame;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@RestController
@RequestMapping("/api/library")
public class UserGameController {
    private final UserGameService userGameService;
    public UserGameController(UserGameService userGameService){
        this.userGameService = userGameService;
    }
    @GetMapping("/users/{userId}")
    public List<UserGame> getLibrary(@PathVariable Long userid){
        return userGameService.getLibrary(userid);
    }
}
