package com.altis.library.auth.filters;

import com.altis.library.auth.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            String tokenSubject = jwtService.validateToken(token);
            String role = jwtService.getRole(token);
            Long userId = jwtService.getUserId(token);
            Long tokenVersion = jwtService.getTokenVersion(token);

            if (tokenSubject != null && !tokenSubject.isBlank()
                    && role != null && userId != null && tokenVersion != null) {
                User user = userRepository.findById(userId).orElse(null);
                boolean tokenMatchesUser = user != null
                        && user.isActive()
                        && user.getTokenVersion() == tokenVersion
                        && user.isAdmin() == "ADMIN".equals(role);
                if (tokenMatchesUser) {
                    String authority = user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority(authority)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
