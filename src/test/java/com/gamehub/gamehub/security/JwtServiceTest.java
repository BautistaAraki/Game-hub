package com.gamehub.gamehub.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final JwtService jwtService = new JwtService(
            userRepository,
            "test-secret-with-enough-length",
            3600
    );

    @Test
    void createTokenCanResolveUserAgain() {
        User user = new User("bauti", "bauti@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String token = jwtService.createToken(user);

        Optional<User> resolvedUser = jwtService.findUserFromToken(token);

        assertTrue(resolvedUser.isPresent());
        assertEquals("bauti@example.com", resolvedUser.get().getEmail());
    }

    @Test
    void rejectsModifiedToken() {
        User user = new User("bauti", "bauti@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", 1L);

        String token = jwtService.createToken(user);
        String modifiedToken = token.substring(0, token.length() - 2) + "xx";

        assertTrue(jwtService.findUserFromToken(modifiedToken).isEmpty());
    }
}
