package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.DTO.LoginRequest;
import com.gamehub.gamehub.DTO.UserResponse;
import com.gamehub.gamehub.exception.InvalidCredentialsException;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService = new AuthService(userRepository, passwordEncoder);

    @Test
    void loginReturnsUserWhenCredentialsAreValid() {
        User user = new User(
                "bauti",
                "bauti@example.com",
                passwordEncoder.encode("Test12345")
        );

        when(userRepository.findByEmail("bauti@example.com"))
                .thenReturn(Optional.of(user));

        UserResponse response = authService.login(
                new LoginRequest("BAUTI@EXAMPLE.COM", "Test12345")
        );

        assertEquals("bauti", response.username());
        assertEquals("bauti@example.com", response.email());
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User(
                "bauti",
                "bauti@example.com",
                passwordEncoder.encode("Test12345")
        );

        when(userRepository.findByEmail("bauti@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("bauti@example.com", "Wrong12345"))
        );
    }
}
