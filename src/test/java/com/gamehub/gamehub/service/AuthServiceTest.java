package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.dto.AuthResponse;
import com.gamehub.gamehub.dto.LoginRequest;
import com.gamehub.gamehub.exception.InvalidCredentialsException;
import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.repository.UserRepository;
import com.gamehub.gamehub.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = Mockito.mock(JwtService.class);
    private final AuthService authService = new AuthService(userRepository, passwordEncoder, jwtService);

    @Test
    void loginReturnsUserWhenCredentialsAreValid() {
        User user = new User(
                "bauti",
                "bauti@example.com",
                passwordEncoder.encode("Test12345")
        );

        when(userRepository.findByEmail("bauti@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.createToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(
                new LoginRequest("BAUTI@EXAMPLE.COM", "Test12345")
        );

        assertEquals("jwt-token", response.token());
        assertEquals("bauti", response.user().username());
        assertEquals("bauti@example.com", response.user().email());
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
