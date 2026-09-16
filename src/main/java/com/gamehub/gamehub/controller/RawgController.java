package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.RawgGameResponse;
import com.gamehub.gamehub.service.RawgService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rawg")
public class RawgController {

    private final RawgService rawgService;

    public RawgController(RawgService rawgService) {
        this.rawgService = rawgService;
    }

    @GetMapping("/games/search")
    public List<RawgGameResponse> searchGames(
            @RequestParam String query
    ) {
        return rawgService.searchGames(query);
    }
}
