package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.DTO.LibraryStatsResponse;
import com.gamehub.gamehub.service.LibraryStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library/users/{userId}/stats")
public class LibraryStatsController {

    private final LibraryStatsService libraryStatsService;

    public LibraryStatsController(LibraryStatsService libraryStatsService) {
        this.libraryStatsService = libraryStatsService;
    }

    @GetMapping
    public LibraryStatsResponse getStats(
            @PathVariable Long userId
    ) {
        return libraryStatsService.getStats(userId);
    }
}
