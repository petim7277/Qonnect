package com.example.qonnect.infrastructure.adapters.output.keycloak;

import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.domain.exceptions.AuthenticationException;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.LoginUserResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.qonnect.domain.validators.InputValidator.validateInput;


@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakAdapter implements IdentityManagementOutputPort {


    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${app.keycloak.tokenUrl}")
    private String tokenUrl;


    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${app.keycloak.logouturl}")
    private String logoutUrl;

    private final Keycloak keycloak;

    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;


    @Override
    public User createUser(User user) throws IdentityManagementException, UserAlreadyExistException {
        if (doesUserExist(user.getEmail())) {
            throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY, HttpStatus.CONFLICT);
        }
        UserRepresentation userRepresentation = createUserRepresentation(user);
        log.info("Using realm: {}", keycloak.realm(realm).toRepresentation().getRealm());
        try (Response response = getUserResource().create(userRepresentation)) {
            log.info("Keycloak user creation response status: {}, body: {}", response.getStatus(), response.readEntity(String.class));
            log.info("Sent to Keycloak: {}", userRepresentation);

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                UserRepresentation createdUser = getUserById(userId).toRepresentation();
                log.info("Created user enabled: {}, emailVerified: {}", createdUser.isEnabled(), createdUser.isEmailVerified());
                assignRole(userId, user.getRole().name());
                user.setKeycloakId(userId);
                return user;
            } else {
                String errorMessage = response.readEntity(String.class);
                log.error("Keycloak user creation failed. Status: {}, Error: {}", response.getStatus(), errorMessage);
                if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY, HttpStatus.CONFLICT);
                } else {
                    throw new IdentityManagementException("Failed to create user in Keycloak: " + errorMessage,HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
    }



    private UserRepresentation findUserByUsername(String username) throws UserNotFoundException {
        List<UserRepresentation> userUsername = getUserResource().search(username);
        if(userUsername == null) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return userUsername.get(0);
    }


    private void assignRole(String userId, String role) throws IdentityManagementException {
        UserResource usersResource = getUserById(userId);
        RoleRepresentation roleRepresentation = getRolesResource().get(role).toRepresentation();
        if (roleRepresentation == null) {
            throw new IdentityManagementException(ErrorMessages.roleNotFound(role),HttpStatus.NOT_FOUND);
        }
        usersResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
    }

    private RolesResource getRolesResource() {
        return keycloak.realm(realm).roles();
    }

    private UserResource getUserById(String userId) {
        return getUserResource().get(userId);
    }

    private UsersResource getUserResource() {
        return keycloak.realm(realm).users();
    }

    private UserRepresentation createUserRepresentation(User user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(user.getEmail());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false);
        userRepresentation.setCredentials(List.of(createPasswordCredentials(user.getPassword())));
        return userRepresentation;
    }

    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        credentialRepresentation.setTemporary(false);
        return credentialRepresentation;
    }


    @Override
    public boolean doesUserExist(String email) {
        validateInput(email);
        List<UserRepresentation> userRepresentations = getUserResource().list();
        for (UserRepresentation user : userRepresentations) {
            if (user.getUsername().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public User login(User user) throws AuthenticationException {
        try {
            ResponseEntity<String> response = authenticateUserWithKeycloak(user);
            log.info("Response from auth server: {}", response.getBody());

            LoginUserResponse loginUserResponse = objectMapper.readValue(response.getBody(), LoginUserResponse.class);

            user.setAccessToken(loginUserResponse.getAccessToken());
            user.setRefreshToken(loginUserResponse.getRefreshToken());
            user.setScope(loginUserResponse.getScope());
            user.setExpiresIn(loginUserResponse.getExpiresIn());
            user.setRefreshExpiresIn(loginUserResponse.getRefreshExpiresIn());
            user.setTokenType(loginUserResponse.getTokenType());


            return user;

        } catch (JsonProcessingException e) {
            log.error("Error parsing Keycloak response: ", e);
            throw new AuthenticationException("Failed to process login response", HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (HttpClientErrorException e) {
            log.error("Error authenticating user: {}", e.getResponseBodyAsString());
            throw new AuthenticationException(ErrorMessages.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<String> authenticateUserWithKeycloak(User user) {
        log.info("Attempting Keycloak auth for: {}", user.getEmail());
        log.debug("Using client_id: {}, tokenUrl: {}", clientId, tokenUrl);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("username", user.getEmail());
        params.add("password", user.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            log.info("Keycloak response status: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Keycloak error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }


    @Override
    public void deleteUser(User user) throws UserNotFoundException {

        UserRepresentation  username = findUserByUsername(user.getEmail());
        keycloak.realm(realm).users().get(username.getId()).remove();
    }


    @Override
    public Optional<User> getUserByEmail(String email) throws UserNotFoundException {
        log.info("passed email::{}", email);
        UserRepresentation userRepresentation = findUserByUsername(email);
        if (userRepresentation == null) {
            return Optional.empty();
        }
        User user = userMapper.toDomain(userRepresentation);
        log.info("Found user in keycloak::=====>>> {}", user);
        user.setUserRepresentation(userRepresentation);
        return Optional.of(user);
    }


    public void changePassword(User userIdentity) {
        if (!confirmValidLoginDetails(userIdentity)) {
            throw new IdentityManagementException("Invalid current password", HttpStatus.BAD_REQUEST);
        }

        Keycloak keycloak = getKeycloakAdmin();

        UserRepresentation user = keycloak.realm(realm)
                .users()
                .search(userIdentity.getEmail())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IdentityManagementException("User not found", HttpStatus.NOT_FOUND));

        CredentialRepresentation newCred = new CredentialRepresentation();
        newCred.setTemporary(false);
        newCred.setType(CredentialRepresentation.PASSWORD);
        newCred.setValue(userIdentity.getNewPassword());

        keycloak.realm(realm).users().get(user.getId()).resetPassword(newCred);
    }


    public boolean confirmValidLoginDetails(User user) {
        try {

            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .grantType("password")
                    .build();

            AccessTokenResponse token = keycloak.tokenManager().getAccessToken();
            return token != null && !token.getToken().isEmpty();

        } catch (Exception e) {
            return false;
        }
    }


    private Keycloak getKeycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials")
                .build();
    }

//    private Keycloak getKeycloakWithUserCredentials(User userIdentity) {
//        return KeycloakBuilder.builder()
//                .serverUrl(serverUrl)
//                .realm(realm)
//                .clientId(clientId)
//                .clientSecret(clientSecret)
//                .grantType("password")
//                .username(userIdentity.getEmail())
//                .password(userIdentity.getPassword())
//                .build();
//    }






    @Override
    public void resetPassword(User user) throws AuthenticationException {
        try {
            UserRepresentation foundUser = findUserByUsername(user.getEmail());
            CredentialRepresentation credential = createPasswordCredentials(user.getPassword());

            getUserById(foundUser.getId()).resetPassword(credential);

        } catch (UserNotFoundException e) {
            throw new AuthenticationException(ErrorMessages.USER_NOT_FOUND,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            throw new AuthenticationException(ErrorMessages.PASSWORD_RESET_FAILED,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public UserResource getUserResourceById(String userId) throws IdentityManagementException {
        log.info("user id:{}", userId);
        try {
            UserResource userRepresentation = keycloak.realm(realm).users().get(userId);
            log.info("user resource::{}", userRepresentation.toRepresentation().getEmail());
            return userRepresentation;
        } catch (Exception e) {
            throw new IdentityManagementException(ErrorMessages.ERROR_FETCHING_USER_INFORMATION, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public void logout(User user, String refreshToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(logoutUrl, request, String.class);
            log.info("Logout success for user {}: status={}", user.getEmail(), response.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("Logout failed for user {}: status={}, body={}",
                    user.getEmail(), e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    @Override
    public void activateUser(User user) {
        UserRepresentation userRep = findUserByUsername(user.getEmail());
        userRep.setEnabled(true);

        UserResource userResource = keycloak.realm(realm)
                .users()
                .get(userRep.getId());

        userResource.update(userRep);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(user.getPassword());

        userResource.resetPassword(credential);
    }


}
