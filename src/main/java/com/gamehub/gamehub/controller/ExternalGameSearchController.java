package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.AddExternalGameToLibraryRequest;
import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import com.gamehub.gamehub.dto.UserGameResponse;
import com.gamehub.gamehub.service.ExternalGameSearchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external-games")
public class ExternalGameSearchController {

    private final ExternalGameSearchService externalGameSearchService;

    public ExternalGameSearchController(ExternalGameSearchService externalGameSearchService) {
        this.externalGameSearchService = externalGameSearchService;
    }

    @GetMapping("/search")
    public List<ExternalGameSearchResponse> searchGames(
            @RequestParam String query
    ) {
        return externalGameSearchService.searchGames(query);
    }

    @PostMapping("/library")
    public UserGameResponse addToLibrary(
            @Valid @RequestBody AddExternalGameToLibraryRequest request
    ) {
        return externalGameSearchService.addToLibrary(request);
    }
}
