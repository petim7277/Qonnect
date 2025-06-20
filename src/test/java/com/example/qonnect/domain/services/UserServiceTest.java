package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Stream;

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
                .password("secret")
                .email("praise@example.com")
                .firstName("Praise")
                .lastName("Oyewole")
                .role(Role.QA_ENGINEER)
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
        verify(otpService).createOtp(user.getFirstName(), user.getEmail(), OtpType.VERIFICATION);
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
        assertEquals(ErrorMessages.EMPTY_INPUT_ERROR, ex.getMessage());
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