package com.gamehub.gamehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamehub.gamehub.DTO.LinkSteamAccountRequest;
import com.gamehub.gamehub.DTO.UserExternalAccountResponse;
import com.gamehub.gamehub.exception.ExternalAccountAlreadyLinkedException;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
import com.gamehub.gamehub.mode1.Platform;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.mode1.UserExternalAccount;
import com.gamehub.gamehub.repository.UserExternalAccountRepository;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class UserExternalAccountServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserExternalAccountRepository userExternalAccountRepository =
            Mockito.mock(UserExternalAccountRepository.class);
    private final UserExternalAccountService userExternalAccountService =
            new UserExternalAccountService(
                    userRepository,
                    userExternalAccountRepository
            );

    @Test
    void linkSteamAccountCreatesExternalAccount() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userExternalAccountRepository.save(Mockito.any(UserExternalAccount.class)))
                .thenAnswer(invocation -> {
                    UserExternalAccount account = invocation.getArgument(0);
                    ReflectionTestUtils.setField(account, "id", 10L);
                    return account;
                });

        UserExternalAccountResponse response =
                userExternalAccountService.linkSteamAccount(
                        1L,
                        new LinkSteamAccountRequest(" 76561198000000000 ")
                );

        ArgumentCaptor<UserExternalAccount> accountCaptor =
                ArgumentCaptor.forClass(UserExternalAccount.class);
        verify(userExternalAccountRepository).save(accountCaptor.capture());

        UserExternalAccount savedAccount = accountCaptor.getValue();
        assertEquals(10L, response.id());
        assertEquals(1L, response.userId());
        assertEquals(Platform.STEAM, response.platform());
        assertEquals("76561198000000000", response.externalUserId());
        assertEquals("76561198000000000", savedAccount.getExternalUserId());
    }

    @Test
    void linkSteamAccountRejectsUnknownUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userExternalAccountService.linkSteamAccount(
                        99L,
                        new LinkSteamAccountRequest("76561198000000000")
                )
        );

        verify(userExternalAccountRepository, never())
                .save(Mockito.any(UserExternalAccount.class));
    }

    @Test
    void linkSteamAccountRejectsUserThatAlreadyHasSteamLinked() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userExternalAccountRepository.existsByUser_IdAndPlatform(1L, Platform.STEAM))
                .thenReturn(true);

        assertThrows(
                ExternalAccountAlreadyLinkedException.class,
                () -> userExternalAccountService.linkSteamAccount(
                        1L,
                        new LinkSteamAccountRequest("76561198000000000")
                )
        );

        verify(userExternalAccountRepository, never())
                .save(Mockito.any(UserExternalAccount.class));
    }

    @Test
    void linkSteamAccountRejectsSteamAccountLinkedToAnotherUser() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userExternalAccountRepository.existsByPlatformAndExternalUserId(
                Platform.STEAM,
                "76561198000000000"
        )).thenReturn(true);

        assertThrows(
                ExternalAccountAlreadyLinkedException.class,
                () -> userExternalAccountService.linkSteamAccount(
                        1L,
                        new LinkSteamAccountRequest("76561198000000000")
                )
        );

        verify(userExternalAccountRepository, never())
                .save(Mockito.any(UserExternalAccount.class));
    }

    private User user(Long id) {
        User user = new User("bauti", "bauti@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
