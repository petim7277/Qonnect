package com.example.qonnect.domain.models;

import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.regex.Pattern;

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
    private String tokenType;
    private boolean enabled;
    private String idToken;
    private String newPassword;
    private Organization organization;
    private String scope;
    @JsonProperty("access_token")
    protected String accessToken;
    @JsonProperty("expires_in")
    protected long expiresIn;
    @JsonProperty("refresh_expires_in")
    protected long refreshExpiresIn;
    @JsonProperty("refresh_token")
    protected String refreshToken;
    private String email;
    private UserRepresentation userRepresentation;
    private String keycloakId;
    private String password;
    private List<Project> projects;
    private Role role;




    public static void validateUserDetails(User user) {

       validateEmail(user.getEmail());
       validatePassword(user.getPassword());
       validateName(user.getFirstName(), "First name");
       validateName(user.getLastName(), "Last name");
       validateRole(user.getRole() != null ? user.getRole().name() : null);

    }


}



