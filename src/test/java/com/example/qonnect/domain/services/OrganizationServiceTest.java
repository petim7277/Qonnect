package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.EmailOutputPort;
import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.OrganizationOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationAlreadyExistsException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.enums.OtpType;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizationServiceTest {

    @Mock private UserOutputPort userOutputPort;
    @Mock private OrganizationOutputPort organizationOutputPort;
    @Mock private IdentityManagementOutputPort identityManagementOutputPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OtpService otpService;
    @Mock private EmailOutputPort emailOutputPort;

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
                otpService,
                emailOutputPort
        );

        org = Organization.builder()
                .id(100L)
                .name("Semicolon Tech")
                .build();

        user = User.builder()
                .id(1L)
                .email("praise@example.com")
                .firstName("Praise")
                .lastName("Oyewole")
                .password("Password123@")
                .role(Role.ADMIN)
                .organization(org)
                .build();
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
    void testInvalidOrganizationInput(String input) {
        Organization organization = Organization.builder().name(input).build();
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerOrganizationAdmin(user, organization));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void testInvalidUserInput(String input) {
        User invalidUser = User.builder()
                .firstName(input)
                .lastName(input)
                .email(input)
                .password(input)
                .build();
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerOrganizationAdmin(invalidUser, org));
    }

    @Test
    void shouldInviteUserSuccessfully() {
        String inviteeEmail = "invitee@example.com";

        User inviter = User.builder()
                .email("admin@qonnect.com")
                .role(Role.ADMIN)
                .organization(org)
                .build();

        when(userOutputPort.getUserByEmail(inviter.getEmail())).thenReturn(inviter);
        when(userOutputPort.userExistsByEmail(inviteeEmail)).thenReturn(false);

        registrationService.inviteUser(inviter, inviteeEmail, Role.QA_ENGINEER);

        verify(userOutputPort).saveUser(argThat(invitee ->
                inviteeEmail.equals(invitee.getEmail()) &&
                        invitee.getRole() == Role.QA_ENGINEER &&
                        invitee.isInvited() &&
                        !invitee.isEnabled() &&
                        invitee.getOrganization().equals(org) &&
                        invitee.getInviteToken() != null
        ));
        verify(emailOutputPort).sendEmail(eq(inviteeEmail), contains("You're Invited"), anyString());
    }

    @Test
    void shouldThrowWhenInviterIsNotAdmin() {
        User inviter = User.builder()
                .email("tester@qonnect.com")
                .role(Role.QA_ENGINEER)
                .build();

        when(userOutputPort.getUserByEmail(inviter.getEmail())).thenReturn(inviter);

        assertThrows(AccessDeniedException.class, () ->
                registrationService.inviteUser(inviter, "new@qonnect.com", Role.QA_ENGINEER));
    }

    @Test
    void shouldThrowWhenInviteeAlreadyExists() {
        User inviter = User.builder()
                .email("admin@qonnect.com")
                .role(Role.ADMIN)
                .organization(org)
                .build();

        when(userOutputPort.getUserByEmail(inviter.getEmail())).thenReturn(inviter);
        when(userOutputPort.userExistsByEmail("dup@qonnect.com")).thenReturn(true);

        assertThrows(UserAlreadyExistException.class, () ->
                registrationService.inviteUser(inviter, "dup@qonnect.com", Role.QA_ENGINEER));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void shouldThrowWhenInviteeEmailIsInvalid(String invalidEmail) {
        User inviter = User.builder()
                .email("admin@qonnect.com")
                .role(Role.ADMIN)
                .organization(org)
                .build();

        when(userOutputPort.getUserByEmail(inviter.getEmail())).thenReturn(inviter);

        assertThrows(IllegalArgumentException.class, () ->
                registrationService.inviteUser(inviter, invalidEmail, Role.QA_ENGINEER));
    }

    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }

    @Test
    void shouldRemoveUserFromOrganizationSuccessfully() {
        User admin = User.builder().id(1L).role(Role.ADMIN).build();
        User userToRemove = User.builder().id(2L).build();

        Organization organization = Organization.builder()
                .id(10L)
                .users(List.of(admin, userToRemove))
                .build();

        when(userOutputPort.existById(admin.getId())).thenReturn(true);
        when(userOutputPort.getUserById(userToRemove.getId())).thenReturn(userToRemove);
        when(organizationOutputPort.getOrganizationById(organization.getId())).thenReturn(organization);

        registrationService.removeUserFromAnOrganization(admin, userToRemove.getId(), organization.getId());

        verify(organizationOutputPort).removeUserFromOrganization(userToRemove, organization);
    }

    @Test
    void shouldThrowAccessDeniedIfNotAdmin() {
        User nonAdmin = User.builder().id(1L).role(Role.QA_ENGINEER).build();
        when(userOutputPort.existById(nonAdmin.getId())).thenReturn(true);

        assertThrows(AccessDeniedException.class, () ->
                registrationService.removeUserFromAnOrganization(nonAdmin, 2L, 10L));
    }

    @Test
    void shouldNotRemoveIfUsersNotInSameOrganization() {
        User admin = User.builder().id(1L).role(Role.ADMIN).build();
        User outsider = User.builder().id(3L).build();

        Organization organization = Organization.builder()
                .id(10L)
                .users(List.of(admin))
                .build();

        when(userOutputPort.existById(admin.getId())).thenReturn(true);
        when(organizationOutputPort.getOrganizationById(organization.getId())).thenReturn(organization);
        when(userOutputPort.getUserById(outsider.getId())).thenReturn(outsider);

        registrationService.removeUserFromAnOrganization(admin, outsider.getId(), organization.getId());

        verify(organizationOutputPort, never()).removeUserFromOrganization(any(), any());
    }

    @Test
    void getAllUsersInOrganization_shouldReturnPageOfUsers_WhenUserIsAdminAndExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(List.of(user));

        when(userOutputPort.existById(user.getId())).thenReturn(true);
        when(userOutputPort.findAllByOrganizationId(org.getId(), pageable)).thenReturn(expectedPage);

        Page<User> result = registrationService.getAllUsersInOrganization(user, org.getId(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
    }

    @Test
    void getAllUsersInOrganization_shouldThrow_WhenUserDoesNotExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userOutputPort.existById(user.getId())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () ->
                registrationService.getAllUsersInOrganization(user, org.getId(), pageable));
    }

    @Test
    void getAllUsersInOrganization_shouldThrow_WhenUserIsNotAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        user.setRole(Role.DEVELOPER);
        when(userOutputPort.existById(user.getId())).thenReturn(true);

        assertThrows(AccessDeniedException.class, () ->
                registrationService.getAllUsersInOrganization(user, org.getId(), pageable));
    }
}
