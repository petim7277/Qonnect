package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.OrganizationOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationAlreadyExistsException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.models.Organization;
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

class OrganizationServiceTest {

    @Mock private UserOutputPort userOutputPort;
    @Mock private OrganizationOutputPort organizationOutputPort;
    @Mock private IdentityManagementOutputPort identityManagementOutputPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OtpService otpService;

    @InjectMocks
    private OrganizationService registrationService;

    private User user;
    private Organization org;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registrationService = new OrganizationService(
                userOutputPort,
                organizationOutputPort,
                identityManagementOutputPort,
                passwordEncoder,
                otpService
        );

        user = new User();
        user.setEmail("praise@example.com");
        user.setFirstName("Praise");
        user.setLastName("Oyewole");
        user.setPassword("Password123@");

        org = new Organization();
        org.setId(100L);
        org.setName("Semicolon Tech");
    }

    @Test
    void shouldRegisterOrganizationAdminSuccessfully() {
        when(userOutputPort.userExistsByEmail(user.getEmail())).thenReturn(false);
        when(organizationOutputPort.existsByName(org.getName())).thenReturn(false);
        when(organizationOutputPort.saveOrganization(any())).thenReturn(org);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(identityManagementOutputPort.createUser(any())).thenReturn(user);
        when(userOutputPort.saveUser(any())).thenReturn(user);

        User result = registrationService.registerOrganizationAdmin(user, org);

        assertNotNull(result);
        assertEquals("praise@example.com", result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
        verify(otpService).createOtp(user.getFirstName(), user.getEmail(), OtpType.VERIFICATION);
    }

    @Test
    void shouldThrowWhenUserAlreadyExists() {
        when(userOutputPort.userExistsByEmail(user.getEmail())).thenReturn(true);

        UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class,
                () -> registrationService.registerOrganizationAdmin(user, org));

        assertEquals(ErrorMessages.USER_EXISTS_ALREADY, exception.getMessage());
        verify(userOutputPort).userExistsByEmail(user.getEmail());
        verifyNoMoreInteractions(userOutputPort, organizationOutputPort, identityManagementOutputPort);
    }

    @Test
    void shouldThrowWhenOrganizationAlreadyExists() {
        when(userOutputPort.userExistsByEmail(user.getEmail())).thenReturn(false);
        when(organizationOutputPort.existsByName(org.getName())).thenReturn(true);

        OrganizationAlreadyExistsException exception = assertThrows(OrganizationAlreadyExistsException.class,
                () -> registrationService.registerOrganizationAdmin(user, org));

        assertEquals(ErrorMessages.ORGANIZATION_ALREADY_EXISTS, exception.getMessage());
        verify(organizationOutputPort).existsByName(org.getName());
        verifyNoMoreInteractions(identityManagementOutputPort);
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testInvalidOrganizationInput(String input){
        Organization organization = new Organization();
        organization.setName(input);
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerOrganizationAdmin(user, organization));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testInvalidUserInput(String input){
        User user1 = new User();
        user1.setFirstName(input);
        user1.setLastName(input);
        user1.setEmail(input);
        user1.setPassword(input);
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerOrganizationAdmin(user1, org));
    }

    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }




}
