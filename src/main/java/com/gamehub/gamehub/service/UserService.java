package com.gamehub.gamehub.service;

import com.gamehub.gamehub.DTO.RegisterUserRequest;
import com.gamehub.gamehub.DTO.UserResponse;
import com.gamehub.gamehub.exception.UserAlreadyExistsException;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("El nombre de usuario ya esta en uso");
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("El email ya esta en uso");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(username, email, passwordHash);
        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
