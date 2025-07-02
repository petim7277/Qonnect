package com.example.qonnect.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@Setter
@Getter
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String tokenType;
    private String idToken;
    private String newPassword;
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

}



