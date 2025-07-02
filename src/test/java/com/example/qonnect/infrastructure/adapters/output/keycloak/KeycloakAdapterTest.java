package com.example.qonnect.infrastructure.adapters.output.keycloak;

import static com.example.qonnect.domain.models.Role.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.domain.exceptions.AuthenticationException;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeycloakAdapterTest {

    @Autowired
    private IdentityManagementOutputPort identityPort;

    private User testUser;

    private final List<String> createdEmails = new ArrayList<>();

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
    void testThatChangePasswordFailsWithWrongCurrentPassword() throws Exception {
        identityPort.createUser(testUser);
        createdEmails.add(testUser.getEmail());

        User changeRequest = new User();
        changeRequest.setEmail(testUser.getEmail());
        changeRequest.setPassword("WrongCurrentPassword!");
        changeRequest.setNewPassword("UpdatedPassword456!");

        assertThrows(IdentityManagementException.class, () -> identityPort.changePassword(changeRequest));
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

}
