package com.altis.library.users.services;

import com.altis.library.exceptions.ConflictException;
import com.altis.library.mappers.UserMapper;
import com.altis.library.users.models.dtos.PasswordChangeRequest;
import com.altis.library.users.models.dtos.UserProfileUpdateRequest;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import com.altis.library.loans.repositories.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(
                userRepository,
                passwordEncoder,
                new UserMapper(),
                mock(LoanRepository.class)
        );
        user = new User(
                "Original Name",
                "original@example.com",
                "current-hash",
                "555-0100",
                "12345678900",
                LocalDate.of(1990, 1, 1),
                "Original address"
        );
        user.setId(42L);
    }

    @Test
    void profileUpdateChangesOnlyAllowedFields() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.existsByEmailAndIdNot("new@example.com", user.getId()))
                .thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName("Updated Name");
        request.setEmail("new@example.com");
        request.setPhone("555-0101");
        request.setAddress("Updated address");

        var response = userService.updateMyProfile(user.getEmail(), request);

        assertEquals(42L, response.getId());
        assertEquals("Updated Name", user.getName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("555-0101", user.getPhone());
        assertEquals("Updated address", user.getAddress());
        assertEquals("12345678900", user.getCpf());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthDate());
        assertEquals("current-hash", user.getPassword());
        assertFalse(user.isAdmin());
        assertTrue(user.isActive());
    }

    @Test
    void duplicateEmailIsRejected() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.existsByEmailAndIdNot("taken@example.com", user.getId()))
                .thenReturn(true);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName("Updated Name");
        request.setEmail("taken@example.com");
        request.setPhone("555-0101");
        request.setAddress("Updated address");

        assertThrows(
                ConflictException.class,
                () -> userService.updateMyProfile(user.getEmail(), request)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ownEmailRemainsAllowed() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId()))
                .thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName("Original Name");
        request.setEmail(user.getEmail());
        request.setPhone("555-0100");
        request.setAddress("Original address");

        assertEquals(user.getEmail(), userService.updateMyProfile(user.getEmail(), request).getEmail());
    }

    @Test
    void passwordChangeVerifiesCurrentPasswordAndInvalidatesOldTokenVersion() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.matches("current-password", "current-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "current-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("current-password");
        request.setNewPassword("new-password");
        request.setConfirmPassword("new-password");

        userService.changeMyPassword(user.getEmail(), request);

        assertEquals("new-hash", user.getPassword());
        assertEquals(1L, user.getTokenVersion());
        verify(passwordEncoder).encode("new-password");
    }

    @Test
    void incorrectCurrentPasswordDoesNotChangePassword() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.matches("wrong", "current-hash")).thenReturn(false);

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("new-password");
        request.setConfirmPassword("new-password");

        assertThrows(
                BadCredentialsException.class,
                () -> userService.changeMyPassword(user.getEmail(), request)
        );
        assertEquals("current-hash", user.getPassword());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void passwordConfirmationAndReuseAreRejected() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.matches("current-password", "current-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "current-hash")).thenReturn(false);
        PasswordChangeRequest mismatch = passwordRequest("new-password", "different-password");
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeMyPassword(user.getEmail(), mismatch)
        );

        PasswordChangeRequest reused = passwordRequest("current-password", "current-password");
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeMyPassword(user.getEmail(), reused)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void administratorsAndInactiveUsersCannotUseOwnProfileService() {
        user.setAdmin(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> userService.getMyProfile(user.getEmail())
        );

        user.setAdmin(false);
        user.setActive(false);
        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> userService.getMyProfile(user.getEmail())
        );
    }

    private PasswordChangeRequest passwordRequest(String newPassword, String confirmation) {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("current-password");
        request.setNewPassword(newPassword);
        request.setConfirmPassword(confirmation);
        return request;
    }
}
