package com.gamehub.gamehub.service;

import com.gamehub.gamehub.DTO.LinkSteamAccountRequest;
import com.gamehub.gamehub.DTO.UserExternalAccountResponse;
import com.gamehub.gamehub.exception.ExternalAccountAlreadyLinkedException;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
import com.gamehub.gamehub.mode1.Platform;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.mode1.UserExternalAccount;
import com.gamehub.gamehub.repository.UserExternalAccountRepository;
import com.gamehub.gamehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserExternalAccountService {

    private final UserRepository userRepository;
    private final UserExternalAccountRepository userExternalAccountRepository;

    public UserExternalAccountService(
            UserRepository userRepository,
            UserExternalAccountRepository userExternalAccountRepository
    ) {
        this.userRepository = userRepository;
        this.userExternalAccountRepository = userExternalAccountRepository;
    }

    @Transactional
    public UserExternalAccountResponse linkSteamAccount(
            Long userId,
            LinkSteamAccountRequest request
    ) {
        String steamId = request.steamId().trim();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (userExternalAccountRepository.existsByUser_IdAndPlatform(userId, Platform.STEAM)) {
            throw new ExternalAccountAlreadyLinkedException(
                    "El usuario ya tiene una cuenta de Steam vinculada"
            );
        }

        if (userExternalAccountRepository.existsByPlatformAndExternalUserId(
                Platform.STEAM,
                steamId
        )) {
            throw new ExternalAccountAlreadyLinkedException(
                    "La cuenta de Steam ya esta vinculada a otro usuario"
            );
        }

        UserExternalAccount account = new UserExternalAccount(
                user,
                Platform.STEAM,
                steamId
        );
        UserExternalAccount savedAccount = userExternalAccountRepository.save(account);

        return toResponse(savedAccount);
    }

    private UserExternalAccountResponse toResponse(UserExternalAccount account) {
        return new UserExternalAccountResponse(
                account.getId(),
                account.getUserId(),
                account.getPlatform(),
                account.getExternalUserId()
        );
    }
}
