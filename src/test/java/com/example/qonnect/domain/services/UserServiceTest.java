package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.OtpException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.domain.models.OtpType;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Stream;

import static com.example.qonnect.domain.models.OtpType.VERIFICATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserOutputPort userOutputPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdentityManagementOutputPort identityManagementOutputPort;
    @Mock private OtpService otpService;

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

        when(userOutputPort.getUserByEmail(user.getEmail())).thenReturn(user);
        doNothing().when(otpService).validateOtp(user.getEmail(), otp);
        doNothing().when(identityManagementOutputPort).resetPassword(any(User.class));

        userService.completeReset(user.getEmail(), otp, newPassword);

        assertEquals(newPassword, user.getPassword());
        verify(userOutputPort).getUserByEmail(user.getEmail());
        verify(otpService).validateOtp(user.getEmail(), otp);
        verify(identityManagementOutputPort).resetPassword(user);
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
    void testLogout_CallsIdentityManagementOutputPort() {
        String refreshToken = "sample-refresh-token";

        userService.logout(user, refreshToken);

        verify(identityManagementOutputPort).logout(user, refreshToken);
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
