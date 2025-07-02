package com.example.qonnect.infrastructure.adapters.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtDecoder jwtDecoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("✅ JwtBlacklistFilter HIT");

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);

            try {
                Jwt jwt = jwtDecoder.decode(accessToken);
                String jti = jwt.getClaimAsString("jti");
                log.info("🔍 Checking jti={} against blacklist", jti);

                boolean isBlacklisted = tokenBlacklistService.isBlacklisted(jti);
                log.info("🚨 Is token blacklisted? {}", isBlacklisted);

                if (jti != null && isBlacklisted) {
                    log.warn("🚫 Blocked blacklisted token with jti={}", jti);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Access token is blacklisted");
                    return;
                }
            } catch (JwtException e) {
                log.error("Invalid JWT: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
