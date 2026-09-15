package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.RegisterUserRequest;
import com.gamehub.gamehub.dto.UserResponse;
import com.gamehub.gamehub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse register(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        return userService.register(request);
    }
}
