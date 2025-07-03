package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.OtpException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.enums.OtpType;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.config.security.TokenBlacklistService;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.apache.http.auth.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.stream.Stream;

import static com.example.qonnect.domain.models.enums.OtpType.VERIFICATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserOutputPort userOutputPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdentityManagementOutputPort identityManagementOutputPort;
    @Mock private OtpService otpService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private  JwtDecoder jwtDecoder;
    @Mock private Jwt jwt;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .password("Password@123")
                .email("praiseoyewole560@gmail.com")
                .firstName("Praise")
                .lastName("Oyewole")
                .role(Role.QA_ENGINEER)
                .enabled(false)
                .build();
    }

    @Test
    void testSignUpSuccess() throws Exception {
        when(userOutputPort.userExistsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded-secret");
        when(identityManagementOutputPort.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userOutputPort.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User registered = userService.signUp(user);

        assertNotNull(registered);
        assertEquals(user.getEmail(), registered.getEmail());
        verify(otpService).createOtp(user.getFirstName(), user.getEmail(), VERIFICATION);
    }

    @Test
    void testSignUpFails_UserAlreadyExists() {
        when(userOutputPort.userExistsByEmail(user.getEmail())).thenReturn(true);

        UserAlreadyExistException ex = assertThrows(UserAlreadyExistException.class, () -> userService.signUp(user));
        assertEquals(ErrorMessages.USER_EXISTS_ALREADY, ex.getMessage());
        verify(identityManagementOutputPort, never()).createUser(any());
        verify(userOutputPort, never()).saveUser(any());
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void testSignUpFails_InvalidInput(String input) {
        user.setEmail(input);
        user.setFirstName(input);
        user.setLastName(input);
        user.setPassword(input);
        Role role = safeParseRole(input);
        user.setRole(role);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.signUp(user));
        assertEquals(ErrorMessages.EMPTY_EMAIL, ex.getMessage());
    }

    @Test
    void testVerifyOtpSuccess() {
        String email = user.getEmail();
        String otp = "123456";

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
        doNothing().when(otpService).validateOtp(email, otp);
        when(userOutputPort.saveUser(user)).thenReturn(user);

        userService.verifyOtp(email, otp);

        assertTrue(user.isEnabled());
        verify(userOutputPort).getUserByEmail(email);
        verify(otpService).validateOtp(email, otp);
        verify(userOutputPort).saveUser(user);
    }

    @Test
    void testVerifyOtpFails_InvalidEmail() {
        String invalidEmail = " ";
        String otp = "123456";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.verifyOtp(invalidEmail, otp));
        assertEquals(ErrorMessages.EMPTY_EMAIL, ex.getMessage());

        verifyNoInteractions(userOutputPort, otpService);
    }

    @Test
    void testVerifyOtpFails_InvalidOtp() {
        String email = user.getEmail();
        String invalidOtp = "wrong";

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
        doThrow(new RuntimeException("Invalid OTP")).when(otpService).validateOtp(email, invalidOtp);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.verifyOtp(email, invalidOtp));
        assertEquals("Invalid OTP", ex.getMessage());

        verify(userOutputPort).getUserByEmail(email);
        verify(otpService).validateOtp(email, invalidOtp);
        verify(userOutputPort, never()).saveUser(any());
    }


    @Test
    void testInitiateResetSuccess() {
        String email = user.getEmail();

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
        when(otpService.createOtp(user.getFirstName(), email, OtpType.RESET_PASSWORD))
                .thenReturn(null);
        userService.initiateReset(email);

        verify(userOutputPort).getUserByEmail(email);
        verify(otpService).createOtp(user.getFirstName(), email, OtpType.RESET_PASSWORD);
    }

    @Test
    void testInitiateResetFails_InvalidEmail() {
        String invalidEmail = "";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.initiateReset(invalidEmail));

        assertEquals(ErrorMessages.EMPTY_EMAIL, ex.getMessage());
        verifyNoInteractions(userOutputPort, otpService);
    }

    @Test
    void testCompleteResetSuccess() {
        String newPassword = "NewPassword@123";
        String otp = "123456";
        String encoded = "ENCODED_HASH";

        when(userOutputPort.getUserByEmail(user.getEmail())).thenReturn(user);
        doNothing().when(otpService).validateOtp(user.getEmail(), otp);
        doNothing().when(identityManagementOutputPort).resetPassword(any(User.class));
        when(passwordEncoder.encode(newPassword)).thenReturn(encoded);

        userService.completeReset(user.getEmail(), otp, newPassword);

        assertEquals(encoded, user.getPassword());
        verify(passwordEncoder).encode(newPassword);
    }


    @Test
    void testCompleteResetFails_InvalidOtp() {
        String newPassword = "NewPassword@123";
        String invalidOtp = "000000";

        when(userOutputPort.getUserByEmail(user.getEmail())).thenReturn(user);
        doThrow(new OtpException(ErrorMessages.INVALID_OTP, HttpStatus.BAD_REQUEST))
                .when(otpService).validateOtp(user.getEmail(), invalidOtp);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.completeReset(user.getEmail(), invalidOtp, newPassword));

        assertEquals(ErrorMessages.INVALID_OTP, ex.getMessage());
        verify(otpService).validateOtp(user.getEmail(), invalidOtp);
        verify(identityManagementOutputPort, never()).resetPassword(any());
    }

    @Test
    void testCompleteResetFails_EmptyPassword() {
        String otp = "123456";
        String emptyPassword = " ";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.completeReset(user.getEmail(), otp, emptyPassword));

        assertEquals(ErrorMessages.EMPTY_PASSWORD, ex.getMessage());
        verifyNoInteractions(otpService, identityManagementOutputPort);
    }

    @Test
    void testChangePassword_Success() {
        String email = user.getEmail();
        String oldPassword = "OldPassword@123";
        String newPassword = "NewPassword@456";

        user.setPassword("encoded-old");

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded-new");

        userService.changePassword(email, oldPassword, newPassword);

        assertEquals("encoded-new", user.getPassword());
        assertEquals("encoded-new", user.getNewPassword());

        verify(identityManagementOutputPort).changePassword(user);
        verify(userOutputPort).saveUser(user);
    }

    @Test
    void testChangePassword_Fails_IncorrectOldPassword() {
        String email = user.getEmail();
        String oldPassword = "Wrong@123";
        String newPassword = "NewPassword@456";

        user.setPassword("encoded-correct");

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        IdentityManagementException ex = assertThrows(IdentityManagementException.class, () ->
                userService.changePassword(email, oldPassword, newPassword));

        assertEquals(ErrorMessages.INCORRECT_OLD_PASSWORD, ex.getMessage());

        verify(identityManagementOutputPort, never()).changePassword(any());
        verify(userOutputPort, never()).saveUser(any());
    }

    @Test
    void testChangePassword_Fails_NewPasswordSameAsOld() {
        String email = user.getEmail();
        String password = "SamePassword@123";

        user.setPassword("encoded-pass");

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        IdentityManagementException ex = assertThrows(IdentityManagementException.class, () ->
                userService.changePassword(email, password, password));

        assertEquals(ErrorMessages.NEW_PASSWORD_SAME_AS_OLD, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());

        verify(identityManagementOutputPort, never()).changePassword(any());
        verify(userOutputPort, never()).saveUser(any());
    }

    @Test
    void testChangePassword_Fails_EmptyPassword() {
        String email = user.getEmail();
        String oldPassword = "";
        String newPassword = "";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword(email, oldPassword, newPassword));

        assertEquals(ErrorMessages.EMPTY_PASSWORD, ex.getMessage());
        verifyNoInteractions(identityManagementOutputPort);
        verifyNoInteractions(userOutputPort);
    }




    @Test
    void shouldLogoutAndBlacklistToken() {

        User user = new User();
        user.setEmail("test@example.com");

        String refreshToken = "dummyRefreshToken";
        String accessToken = "dummyAccessToken";
        String jti = "test-jti";
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(300);

        when(jwtDecoder.decode(accessToken)).thenReturn(jwt);
        when(jwt.getClaimAsString("jti")).thenReturn(jti);
        when(jwt.getExpiresAt()).thenReturn(expiry);

        userService.logout(user, refreshToken, accessToken);

        // Then
        verify(identityManagementOutputPort).logout(user, refreshToken);
        verify(jwtDecoder).decode(accessToken);
        verify(tokenBlacklistService).blacklistToken(eq(jti), anyLong());
    }


    @Test
    void testResendOtp_Success() {
        when(userOutputPort.getUserByEmail(user.getEmail())).thenReturn(user);
        when(otpService.createOtp(user.getFirstName(), user.getEmail(), VERIFICATION)).thenReturn(null);

        userService.resendOtp(user.getEmail(),VERIFICATION);

        verify(userOutputPort).getUserByEmail(user.getEmail());
        verify(otpService).resendOtp(user.getFirstName(), user.getEmail(), VERIFICATION);
    }

    @Test
    void testResendOtp_Fails_EmptyEmail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resendOtp(" ", VERIFICATION));

        assertEquals(ErrorMessages.EMPTY_EMAIL, ex.getMessage());
        verifyNoInteractions(userOutputPort, otpService);
    }

    @Test
    void testViewUserProfile_Success() {
        String email = user.getEmail();

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);

        User result = userService.viewUserProfile(email);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userOutputPort).getUserByEmail(email);
    }

    @Test
    void testViewUserProfile_Fails_InvalidEmail() {
        String invalidEmail = " ";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.viewUserProfile(invalidEmail)
        );

        assertEquals(ErrorMessages.EMPTY_EMAIL, ex.getMessage());
        verifyNoInteractions(userOutputPort);
    }


    @Test
    void testLoginSuccess() throws Exception {
        String rawPassword = "Password@123";
        String encodedPassword = "encodedPass";

        User storedUser = new User();
        storedUser.setEmail("test@example.com");
        storedUser.setPassword(encodedPassword);
        storedUser.setEnabled(true);

        User loginRequest = new User();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword(rawPassword);

        when(userOutputPort.getUserByEmail("test@example.com")).thenReturn(storedUser);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);


        User loggedIn = userService.login(loginRequest);

        assertNotNull(loggedIn);
        assertEquals("test@example.com", loggedIn.getEmail());
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
        verify(identityManagementOutputPort).login(storedUser);
    }


    @Test
    void testLoginFails_UserNotFound() {
        User loginRequest = new User();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("Password@1");

        when(userOutputPort.getUserByEmail("nonexistent@example.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.login(loginRequest));
        verify(passwordEncoder, never()).matches(any(), any());
        verify(identityManagementOutputPort, never()).login(any());
    }

    @Test
    void testLoginFails_InvalidPassword() {
        String encodedPassword = "encodedCorrect1@";

        User storedUser = new User();
        storedUser.setEmail("admin@example.com");
        storedUser.setPassword(encodedPassword);
        storedUser.setEnabled(true);

        User loginRequest = new User();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("WrongPassword@1");

        when(userOutputPort.getUserByEmail("admin@example.com")).thenReturn(storedUser);
        when(passwordEncoder.matches("WrongPassword@1", encodedPassword)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
        verify(identityManagementOutputPort, never()).login(any());
    }


    @Test
    void testCompleteInvitation_populatesUserAndSendsOtp() {
        String token = "TOKEN123";
        when(userOutputPort.getUserByInviteToken(token)).thenReturn(user);
        when(userOutputPort.saveUser(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.completeInvitation(token, "Praise", "Tester", "SecurePass123!");

        assertEquals("Praise", user.getFirstName());
        assertEquals("Tester", user.getLastName());
        assertEquals("SecurePass123!", user.getPassword());

        verify(userOutputPort).saveUser(user);
        verify(otpService).createOtp("Praise", user.getEmail(), OtpType.VERIFICATION);
    }

    @Test
    void testVerifyOtpAndActivate_successfullyActivatesUser() {
        String token = "TOKEN123";
        String otp = "123456";

        when(userOutputPort.getUserByInviteToken(token)).thenReturn(user);
        doNothing().when(otpService).validateOtp(user.getEmail(), otp);
        when(userOutputPort.saveUser(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.verifyOtpAndActivate(token, otp);

        assertTrue(user.isEnabled());
        assertFalse(user.isInvited());

        verify(identityManagementOutputPort).createUser(user);
        verify(userOutputPort).saveUser(user);
    }







    private Role safeParseRole(String input) {
        try {
            return input == null ? null : Role.valueOf(input.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }


}
