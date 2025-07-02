package com.example.qonnect.domain.models;

import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.keycloak.representations.idm.UserRepresentation;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.qonnect.domain.validators.InputValidator.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;

    @JsonIgnore
    private String tokenType;

    @JsonIgnore
    private boolean enabled;

    @JsonIgnore
    private String idToken;

    @JsonIgnore
    private boolean verified;

    @JsonIgnore
    private String newPassword;

    @JsonIgnore
    private Organization organization;

    @JsonIgnore
    private String scope;

    @JsonIgnore
    @JsonProperty("access_token")
    protected String accessToken;

    @JsonIgnore
    @JsonProperty("expires_in")
    protected long expiresIn;

    @JsonIgnore
    @JsonProperty("refresh_expires_in")
    protected long refreshExpiresIn;

    @JsonIgnore
    @JsonProperty("refresh_token")
    protected String refreshToken;

    @JsonIgnore
    private boolean invited;

    @JsonIgnore
    private String inviteToken;

    @JsonIgnore
    private LocalDateTime tokenExpiresAt;

    @JsonIgnore
    private UserRepresentation userRepresentation;

    @JsonIgnore
    private String keycloakId;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private List<Project> projects;

    public static void validateUserDetails(User user) {
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
        validateName(user.getFirstName(), "First name");
        validateName(user.getLastName(), "Last name");
        validateRole(user.getRole() != null ? user.getRole().name() : null);
    }
}
