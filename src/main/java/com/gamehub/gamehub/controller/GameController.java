package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.GameResponse;
import com.gamehub.gamehub.service.GameService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameResponse> getCatalog() {
        return gameService.getCatalog();
    }

    @GetMapping("/search")
    public List<GameResponse> searchByName(
            @RequestParam(required = false) String query
    ) {
        return gameService.searchByName(query);
    }
}
