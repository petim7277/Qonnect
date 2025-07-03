package com.example.qonnect.infrastructure.adapters.config.security;

import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthConverterTests {

    @Mock
    private UserOutputPort userOutputPort;

    @InjectMocks
    private JwtAuthConverter jwtAuthConverter;

    private User testUser;
    private Jwt.Builder jwtBuilder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.DEVELOPER);

        jwtBuilder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
    }

    @Test
    @DisplayName("Should convert JWT with email claim to authentication token")
    void convert_WithEmailClaim_ShouldReturnJwtAuthenticationToken() {
        // Arrange
        Jwt jwt = jwtBuilder
                .claim("email", "test@example.com")
                .build();
        
        when(userOutputPort.getUserByEmail("test@example.com")).thenReturn(testUser);

        // Act
        JwtAuthenticationToken result = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getName());
        verify(userOutputPort).getUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should convert JWT with preferred_username claim to authentication token")
    void convert_WithPreferredUsernameClaim_ShouldReturnJwtAuthenticationToken() {
        // Arrange
        Jwt jwt = jwtBuilder
                .claim("preferred_username", "testuser")
                .build();
        
        when(userOutputPort.getUserByEmail("testuser")).thenReturn(testUser);

        // Act
        JwtAuthenticationToken result = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getName());
        verify(userOutputPort).getUserByEmail("testuser");
    }

    @Test
    @DisplayName("Should convert JWT with subject claim to authentication token")
    void convert_WithSubjectClaim_ShouldReturnJwtAuthenticationToken() {
        // Arrange
        Jwt jwt = jwtBuilder
                .subject("subject-id")
                .build();
        
        when(userOutputPort.getUserByEmail("subject-id")).thenReturn(testUser);

        // Act
        JwtAuthenticationToken result = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getName());
        verify(userOutputPort).getUserByEmail("subject-id");
    }

    @Test
    @DisplayName("Should include realm roles in authorities when converting JWT")
    void convert_WithRealmRoles_ShouldIncludeRolesInAuthorities() {
        // Arrange
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("admin", "user"));
        
        Jwt jwt = jwtBuilder
                .claim("email", "test@example.com")
                .claim("realm_access", realmAccess)
                .build();
        
        when(userOutputPort.getUserByEmail("test@example.com")).thenReturn(testUser);

        // Act
        JwtAuthenticationToken result = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        // Assert
        assertNotNull(result);
        Collection<GrantedAuthority> authorities = result.getAuthorities();
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_admin")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_user")));
    }

    @Test
    @DisplayName("Should include resource roles in authorities when converting JWT")
    void convert_WithResourceRoles_ShouldIncludeRolesInAuthorities() {
        // Arrange
        Map<String, Object> accountRoles = new HashMap<>();
        accountRoles.put("roles", Arrays.asList("manager", "viewer"));
        
        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("account", accountRoles);
        
        Jwt jwt = jwtBuilder
                .claim("email", "test@example.com")
                .claim("resource_access", resourceAccess)
                .build();
        
        when(userOutputPort.getUserByEmail("test@example.com")).thenReturn(testUser);

        // Act
        JwtAuthenticationToken result = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        // Assert
        assertNotNull(result);
        Collection<GrantedAuthority> authorities = result.getAuthorities();
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_manager")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_viewer")));
    }

    @Test
    @DisplayName("Should throw AuthenticationCredentialsNotFoundException when user not found")
    void convert_UserNotFound_ShouldThrowAuthenticationCredentialsNotFoundException() {
        // Arrange
        Jwt jwt = jwtBuilder
                .claim("email", "nonexistent@example.com")
                .build();
        
        when(userOutputPort.getUserByEmail("nonexistent@example.com"))
                .thenThrow(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Act & Assert
        AuthenticationCredentialsNotFoundException exception = assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                () -> jwtAuthConverter.convert(jwt)
        );
        
        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userOutputPort).getUserByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should throw AuthenticationCredentialsNotFoundException on generic exception")
    void convert_GenericException_ShouldThrowAuthenticationCredentialsNotFoundException() {
        // Arrange
        Jwt jwt = jwtBuilder
                .claim("email", "test@example.com")
                .build();
        
        when(userOutputPort.getUserByEmail("test@example.com"))
                .thenThrow(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND,HttpStatus.NOT_FOUND));

        // Act & Assert
        AuthenticationCredentialsNotFoundException exception = assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                () -> jwtAuthConverter.convert(jwt)
        );
        
        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userOutputPort).getUserByEmail("test@example.com");
    }
}