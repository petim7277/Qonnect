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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    @Mock private IdentityManagementOutputPort identityManagementOutputPort;
    @Mock private OtpService otpService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private JwtDecoder jwtDecoder;
    @Mock private Jwt jwt;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder(); // ✅ Use real encoder
        userService = new UserService(
                userOutputPort,
                passwordEncoder,
                identityManagementOutputPort,
                otpService,
                jwtDecoder,
                tokenBlacklistService
        );

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

        String encoded = passwordEncoder.encode(newPassword); // ✅ encode directly using real encoder
        user.setPassword(null); // make sure it's not already encoded

        when(userOutputPort.getUserByEmail(user.getEmail())).thenReturn(user);
        doNothing().when(otpService).validateOtp(user.getEmail(), otp);
        doNothing().when(identityManagementOutputPort).resetPassword(any(User.class));

        userService.completeReset(user.getEmail(), otp, newPassword);

        assertTrue(passwordEncoder.matches(newPassword, user.getPassword())); // ✅ validate properly
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

        String encodedOldPassword = passwordEncoder.encode(oldPassword);
        user.setPassword(encodedOldPassword);

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        userService.changePassword(email, oldPassword, newPassword);

        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
        assertEquals(user.getNewPassword(), user.getPassword());

        verify(identityManagementOutputPort).changePassword(user);
        verify(userOutputPort).saveUser(user);
    }


    @Test
    void testChangePassword_Fails_IncorrectOldPassword() {
        String email = user.getEmail();
        String correctEncodedPassword = passwordEncoder.encode("Correct@123");
        String wrongOldPassword = "Wrong@123";
        String newPassword = "NewPassword@456";

        user.setPassword(correctEncodedPassword); // real encoded password

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);

        IdentityManagementException ex = assertThrows(IdentityManagementException.class, () ->
                userService.changePassword(email, wrongOldPassword, newPassword));

        assertEquals(ErrorMessages.INCORRECT_OLD_PASSWORD, ex.getMessage());

        verify(identityManagementOutputPort, never()).changePassword(any());
        verify(userOutputPort, never()).saveUser(any());
    }


    @Test
    void testChangePassword_Fails_NewPasswordSameAsOld() {
        String email = user.getEmail();
        String password = "SamePassword@123";

        user.setPassword(passwordEncoder.encode(password));

        when(userOutputPort.getUserByEmail(email)).thenReturn(user);

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
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User storedUser = new User();
        storedUser.setEmail("test@example.com");
        storedUser.setPassword(encodedPassword);
        storedUser.setEnabled(true);

        User loginRequest = new User();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword(rawPassword);

        when(userOutputPort.getUserByEmail("test@example.com")).thenReturn(storedUser);
        User loggedIn = userService.login(loginRequest);

        assertNotNull(loggedIn);
        assertEquals("test@example.com", loggedIn.getEmail());
        assertTrue(passwordEncoder.matches(rawPassword, storedUser.getPassword()));

        verify(identityManagementOutputPort).login(storedUser);
    }



    @Test
    void testLoginFails_UserNotFound() {
        User loginRequest = new User();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("Password@1");

        when(userOutputPort.getUserByEmail("nonexistent@example.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.login(loginRequest));

        verify(identityManagementOutputPort, never()).login(any());
    }


    @Test
    void testLoginFails_InvalidPassword() {
        String rawPassword = "WrongPassword@1";
        String encodedPassword = passwordEncoder.encode("CorrectPassword@1");

        User storedUser = new User();
        storedUser.setEmail("admin@example.com");
        storedUser.setPassword(encodedPassword);
        storedUser.setEnabled(true);

        User loginRequest = new User();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword(rawPassword);

        when(userOutputPort.getUserByEmail("admin@example.com")).thenReturn(storedUser);
        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));

        verify(identityManagementOutputPort, never()).login(any());
    }



    @Test
    void testCompleteInvitation_populatesUserAndSendsOtp() {
        String token = "TOKEN123";
        String rawPassword = "SecurePass123!";

        when(userOutputPort.getUserByInviteToken(token)).thenReturn(user);
        when(userOutputPort.saveUser(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.completeInvitation(token, "Praise", "Tester", rawPassword);

        assertEquals("Praise", user.getFirstName());
        assertEquals("Tester", user.getLastName());
        assertTrue(passwordEncoder.matches(rawPassword, user.getPassword()));

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
