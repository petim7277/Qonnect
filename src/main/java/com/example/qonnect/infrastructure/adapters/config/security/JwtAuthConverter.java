package com.example.qonnect.infrastructure.adapters.config.security;


import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Configuration
@Component
@Slf4j

public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        try {
            log.info("Converting JWT token. Subject: {}", jwt.getSubject());
            log.info("JWT Claims: {}", jwt.getClaims().keySet());

//            String userIdentifier = getUserIdentifierFromJwt(jwt);
//            log.info("Extracted user identifier: {}", userIdentifier);
//
//            User user = userPersistenceOutputPort.getUserByEmail(userIdentifier);
//            log.info("Found user: {}", user.getEmail());
//
//            Collection<GrantedAuthority> authorities = Stream.concat(
//                    jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
//                    extractResourceRoles(jwt).stream()
//            ).collect(Collectors.toSet());
//
//            log.info("Granted authorities: {}", authorities);
//
//            return new UserAwareJwtAuthenticationToken(jwt, authorities, user);
//
//        } catch (UserNotFoundException e) {
//            log.error("User not found during JWT conversion", e);
//            throw new AuthenticationCredentialsNotFoundException("User not found", e);
//        } catch (Exception e) {
//            log.error("Authentication failed during JWT conversion", e);
//            throw new AuthenticationCredentialsNotFoundException("Authentication failed", e);
        }
        return null;
    }
}

//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
//    private final UserPersistenceOutputPort userPersistenceOutputPort;
//    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//
//    @Override
//    @Transactional(readOnly = true)
//    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
//        try {
//            log.info("Converting JWT token. Subject: {}", jwt.getSubject());
//            log.info("JWT Claims: {}", jwt.getClaims().keySet());
//
//            String userIdentifier = getUserIdentifierFromJwt(jwt);
//            log.info("Extracted user identifier: {}", userIdentifier);
//
//            User user = userPersistenceOutputPort.getUserByEmail(userIdentifier);
//            log.info("Found user: {}", user.getEmail());
//
//            Collection<GrantedAuthority> authorities = Stream.concat(
//                    jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
//                    extractResourceRoles(jwt).stream()
//            ).collect(Collectors.toSet());
//
//            log.info("Granted authorities: {}", authorities);
//
//            return new UserAwareJwtAuthenticationToken(jwt, authorities, user);
//
//        } catch (UserNotFoundException e) {
//            log.error("User not found during JWT conversion", e);
//            throw new AuthenticationCredentialsNotFoundException("User not found", e);
//        } catch (Exception e) {
//            log.error("Authentication failed during JWT conversion", e);
//            throw new AuthenticationCredentialsNotFoundException("Authentication failed", e);
//        }
//    }
//
//    private String getUserIdentifierFromJwt(Jwt jwt) {
//        String email = jwt.getClaim("email");
//        if (email != null) {
//            log.debug("Using email claim: {}", email);
//            return email;
//        }
//
//        String username = jwt.getClaim("preferred_username");
//        if (username != null) {
//            log.debug("Using preferred_username claim: {}", username);
//            return username;
//        }
//
//        String subject = jwt.getClaim(JwtClaimNames.SUB);
//        log.debug("Using subject claim: {}", subject);
//        return subject;
//    }
//
//    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
//        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
//        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
//
//        Collection<GrantedAuthority> allRoles = new ArrayList<>();
//
//        if (resourceAccess != null && resourceAccess.get("account") != null) {
//            Map<String, Object> account = (Map<String, Object>) resourceAccess.get("account");
//            if (account.containsKey("roles")) {
//                Collection<String> resourceRoles = (Collection<String>) account.get("roles");
//                allRoles.addAll(resourceRoles.stream()
//                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                        .collect(Collectors.toList()));
//            }
//        }
//
//        if (realmAccess != null && realmAccess.containsKey("roles")) {
//            Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");
//            allRoles.addAll(realmRoles.stream()
//                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                    .collect(Collectors.toList()));
//        }
//
//        return allRoles;
//    }
//}
