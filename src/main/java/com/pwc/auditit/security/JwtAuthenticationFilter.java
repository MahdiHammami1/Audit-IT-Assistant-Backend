package com.pwc.auditit.security;

import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.repository.ProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ProfileRepository profileRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            request.setAttribute("authError", "Missing Authorization header. Send Authorization: Bearer <token>.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {
                UUID userId = jwtService.extractUserId(token);
                Profile profile = profileRepository.findById(userId)
                        .or(() -> {
                            String email = jwtService.extractEmail(token);
                            return email == null || email.isBlank()
                                    ? java.util.Optional.empty()
                                    : profileRepository.findByEmail(email);
                        })
                        .orElse(null);

                if (profile != null) {
                    List<SimpleGrantedAuthority> authorities = profile.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRole().name()))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(profile, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    request.setAttribute("authError", "Token is valid, but the user no longer exists.");
                }
            } else {
                request.setAttribute("authError", "Invalid or expired authentication token.");
            }
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            request.setAttribute("authError", "Invalid authentication token.");
        }

        filterChain.doFilter(request, response);
    }
}
