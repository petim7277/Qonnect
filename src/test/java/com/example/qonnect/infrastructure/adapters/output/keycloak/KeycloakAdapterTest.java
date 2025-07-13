package com.example.qonnect.infrastructure.adapters.output.keycloak;

import static com.example.qonnect.domain.models.enums.Role.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.domain.exceptions.AuthenticationException;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.models.User;
//import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest

class KeycloakAdapterTest {

    @Autowired
    private IdentityManagementOutputPort identityPort;

    private User testUser;

    private final List<String> createdEmails = new ArrayList<>();
    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;




    @BeforeEach
    void setUp() {
        deleteIfExists("testuser@example.com");

        testUser = new User();
        testUser.setId(1001L);
        testUser.setPassword("TestPassword123!");
        testUser.setEmail("testuser@example.com");
        testUser.setFirstName("Praise");
        testUser.setLastName("Tester");
        testUser.setRole(DEVELOPER);
    }

    @AfterEach
    void tearDown() {
        for (String email : createdEmails) {
            deleteIfExists(email);
        }
        createdEmails.clear();
    }

    private void deleteIfExists(String email) {
        try {
            if (email == null || email.isBlank()) return;
            User tempUser = new User();
            tempUser.setEmail(email);
            identityPort.deleteUser(tempUser);
            System.out.println("Deleted user: " + email);
        } catch (Exception e) {
            System.out.println("Skip deletion, user may not exist yet: " + email);
        }
    }

    @Test
    void shouldCreateUserSuccessfully_whenUserIsValid() {
        User createdUser = identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        assertNotNull(createdUser);
        assertNotNull(createdUser.getKeycloakId());
        assertEquals(testUser.getEmail(), createdUser.getEmail());
        assertEquals(DEVELOPER, createdUser.getRole());
    }

    @Test
    void shouldThrowException_whenUserAlreadyExists() {
        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());
        assertThrows(UserAlreadyExistException.class, () -> identityPort.createUser(testUser));
    }

    @Test
    void shouldThrowIdentityManagerException_whenUserInputIsInvalid() {
        testUser.setEmail("");
        assertThrows(IllegalArgumentException.class, () -> identityPort.createUser(testUser));
    }

    @Test
    void shouldLoginUserSuccessfully_whenCredentialsAreCorrect() {
        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User loginUser = new User();
        loginUser.setEmail(testUser.getEmail());
        loginUser.setPassword(testUser.getPassword());

        User loggedIn = identityPort.login(loginUser);

        assertNotNull(loggedIn.getAccessToken());
        assertNotNull(loggedIn.getRefreshToken());
        assertEquals(testUser.getEmail(), loggedIn.getEmail());
    }

    @Test
    void shouldThrowAuthenticationException_whenPasswordIsIncorrect() {
        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User loginUser = new User();
        loginUser.setEmail(testUser.getEmail());
        loginUser.setPassword("WrongPassword!");

        assertThrows(AuthenticationException.class, () -> identityPort.login(loginUser));
    }

    @Test
    void shouldThrowAuthenticationException_whenEmailDoesNotExist() {
        User loginUser = new User();
        loginUser.setEmail("nonexistent@example.com");
        loginUser.setPassword("AnyPassword123!");

        assertThrows(AuthenticationException.class, () -> identityPort.login(loginUser));
    }

    @Test
    void testThatUserCanResetPassword() throws Exception {

        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User resetRequest = new User();
        resetRequest.setEmail(testUser.getEmail());
        resetRequest.setPassword("newSecurePassword123!");

        assertDoesNotThrow(() -> identityPort.resetPassword(resetRequest));
    }

    @Test
    public void testThatUserCanResetPassword_andCanLoginWithIt() throws Exception {


        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User resetRequest = new User();
        resetRequest.setEmail(testUser.getEmail());
        resetRequest.setPassword("newSecurePassword123!");

        identityPort.resetPassword(resetRequest);

        User loginAttempt = new User();
        loginAttempt.setEmail(testUser.getEmail());
        loginAttempt.setPassword("newSecurePassword123!");
        User loggedInUser = identityPort.login(loginAttempt);

        assertNotNull(loggedInUser.getAccessToken());
    }

    @Test
    public void testThatUserCanResetPassword_LoginWithOldPassword_ThrowException() throws Exception {

        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User resetRequest = new User();
        resetRequest.setEmail(testUser.getEmail());
        resetRequest.setPassword("newSecurePassword123!");

        identityPort.resetPassword(resetRequest);

        User loginAttempt = new User();
        loginAttempt.setEmail(testUser.getEmail());
        loginAttempt.setPassword("password");

        assertThrows(AuthenticationException.class, () -> identityPort.login(loginAttempt));
    }

    @Test
    void testThatChangePasswordRequiresCorrectCurrentPassword() throws Exception {
        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User changeRequest = new User();
        changeRequest.setEmail(testUser.getEmail());
        changeRequest.setPassword("TestPassword123!"); // current password
        changeRequest.setNewPassword("UpdatedPassword456!");

        assertDoesNotThrow(() -> identityPort.changePassword(changeRequest));

        User loginWithNewPassword = new User();
        loginWithNewPassword.setEmail(testUser.getEmail());
        loginWithNewPassword.setPassword("UpdatedPassword456!");

        User loggedIn = identityPort.login(loginWithNewPassword);
        assertNotNull(loggedIn.getAccessToken());
    }



    @Test
    void testThatUserCanLogoutSuccessfully() {
        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User loginUser = new User();
        loginUser.setEmail(testUser.getEmail());
        loginUser.setPassword(testUser.getPassword());

        User loggedIn = identityPort.login(loginUser);

        assertNotNull(loggedIn.getRefreshToken());

        assertDoesNotThrow(() -> identityPort.logout(testUser, loggedIn.getRefreshToken()));


    }

    @Test
    void testActivateUser_enablesUserAndSetsPassword() throws Exception {

        User user = new User();
        user.setEmail("invitee@example.com");
        user.setPassword("NewStrongPass123!");

        Keycloak mockKeycloak          = mock(Keycloak.class);
        RealmResource mockRealm        = mock(RealmResource.class);
        UsersResource mockUsers        = mock(UsersResource.class);
        UserResource  mockUserResource = mock(UserResource.class);

        String kcUserId = "abc‑123‑kc‑id";
        UserRepresentation kcUserRep = new UserRepresentation();
        kcUserRep.setId(kcUserId);
        kcUserRep.setEmail(user.getEmail());

        when(mockKeycloak.realm(anyString())).thenReturn(mockRealm);
        when(mockRealm.users()).thenReturn(mockUsers);
        when(mockUsers.search(eq(user.getEmail()))).thenReturn(List.of(kcUserRep));
        when(mockUsers.get(kcUserId)).thenReturn(mockUserResource);

        doNothing().when(mockUserResource).update(any(UserRepresentation.class));
        doNothing().when(mockUserResource).resetPassword(any(CredentialRepresentation.class));

        KeycloakAdapter adapter = (KeycloakAdapter) identityPort;
        org.springframework.test.util.ReflectionTestUtils
                .setField(adapter, "keycloak", mockKeycloak);

        adapter.activateUser(user);

        verify(mockUsers).get(kcUserId);
        verify(mockUserResource).update(kcUserRep);
        verify(mockUserResource).resetPassword(any(CredentialRepresentation.class));
    }





}
