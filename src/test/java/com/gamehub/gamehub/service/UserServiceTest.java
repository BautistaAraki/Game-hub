package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.DTO.RegisterUserRequest;
import com.gamehub.gamehub.DTO.UserResponse;
import com.gamehub.gamehub.exception.UserAlreadyExistsException;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService = new UserService(userRepository, passwordEncoder);

    @Test
    void registerHashesPasswordAndDoesNotReturnIt() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Bauti",
                "BAUTI@EXAMPLE.COM",
                "Test12345"
        );

        when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Bauti", response.username());
        assertEquals("bauti@example.com", response.email());
        assertEquals("bauti@example.com", savedUser.getEmail());
        assertNotEquals("Test12345", savedUser.getPasswordHash());
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Bauti",
                "bauti@example.com",
                "Test12345"
        );

        when(userRepository.existsByEmail("bauti@example.com")).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(Mockito.any(User.class));
    }
}
