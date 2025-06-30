package com.example.qonnect.infrastructure.adapters.config.security;

import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Configuration
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserOutputPort userOutputPort;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        try {
            log.info("Converting JWT token. Subject: {}", jwt.getSubject());
            log.debug("JWT Claims: {}", jwt.getClaims());

            String userIdentifier = getUserIdentifierFromJwt(jwt);
            log.info("Extracted user identifier: {}", userIdentifier);

            User user = userOutputPort.getUserByEmail(userIdentifier);
            log.info("Found user: {}", user.getEmail());

            Collection<GrantedAuthority> authorities = Stream.concat(
                    jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                    extractRolesFromJwt(jwt).stream()
            ).collect(Collectors.toSet());

            log.debug("Authorities: {}", authorities);

            String jti = jwt.getClaimAsString("jti");
            UserAwareJwtAuthenticationToken auth = new UserAwareJwtAuthenticationToken(jwt, authorities, user);
            auth.setDetails(jti);
            return auth;

        } catch (UserNotFoundException ex) {
            log.error("User not found during JWT conversion", ex);
            throw new AuthenticationCredentialsNotFoundException(ErrorMessages.USER_NOT_FOUND, ex);
        }
    }

    private String getUserIdentifierFromJwt(Jwt jwt) {
        String email = jwt.getClaim("email");
        if (email != null) {
            return email;
        }

        String username = jwt.getClaim("preferred_username");
        if (username != null) {
            return username;
        }

        return jwt.getClaim(JwtClaimNames.SUB);
    }

    private Collection<GrantedAuthority> extractRolesFromJwt(Jwt jwt) {
        Set<GrantedAuthority> roles = new HashSet<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            roles.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet()));
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((client, value) -> {
                Map<String, Object> clientData = (Map<String, Object>) value;
                if (clientData.containsKey("roles")) {
                    List<String> clientRoles = (List<String>) clientData.get("roles");
                    roles.addAll(clientRoles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toSet()));
                }
            });
        }

        return roles;
    }
}
