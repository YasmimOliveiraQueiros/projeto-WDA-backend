package com.altis.library.auth.filters;

import com.altis.library.auth.services.JwtService;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final byte[] key = "01234567890123456789012345678901".getBytes();
    private final JwtService jwtService = new JwtService(new SecretKeySpec(key, "HmacSHA256"));
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService, userRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void emailChangeKeepsTokenBoundToSameUserAndUsesCurrentPrincipal() throws Exception {
        User user = user("new@example.com", 0L, true);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        String token = jwtService.generateToken("old@example.com", false, 42L, 0L);

        authenticate(token);

        assertEquals(
                "new@example.com",
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    @Test
    void changedTokenVersionInvalidatesPreviouslyIssuedToken() throws Exception {
        when(userRepository.findById(42L))
                .thenReturn(Optional.of(user("user@example.com", 1L, true)));
        String token = jwtService.generateToken("user@example.com", false, 42L, 0L);

        authenticate(token);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void inactiveUserCannotUsePreviouslyIssuedToken() throws Exception {
        when(userRepository.findById(42L))
                .thenReturn(Optional.of(user("user@example.com", 0L, false)));
        String token = jwtService.generateToken("user@example.com", false, 42L, 0L);

        authenticate(token);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private void authenticate(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        filter.doFilter(
                request,
                new MockHttpServletResponse(),
                new MockFilterChain()
        );
    }

    private User user(String email, long tokenVersion, boolean active) {
        User user = new User(
                "Test User",
                email,
                "hash",
                "555-0100",
                "12345678900",
                LocalDate.of(1990, 1, 1),
                "Test address"
        );
        user.setId(42L);
        user.setTokenVersion(tokenVersion);
        user.setActive(active);
        return user;
    }
}
