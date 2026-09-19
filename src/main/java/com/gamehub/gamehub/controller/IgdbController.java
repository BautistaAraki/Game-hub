package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.IgdbGameResponse;
import com.gamehub.gamehub.service.IgdbService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/igdb")
public class IgdbController {

    private final IgdbService igdbService;

    public IgdbController(IgdbService igdbService) {
        this.igdbService = igdbService;
    }

    @GetMapping("/games/search")
    public List<IgdbGameResponse> searchGames(
            @RequestParam String query
    ) {
        return igdbService.searchGames(query);
    }
}
