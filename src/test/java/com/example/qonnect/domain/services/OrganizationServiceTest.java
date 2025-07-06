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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        user = new User();
        user.setId(1L);
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


    @Test
    void shouldInviteUserSuccessfully() {
        User inviter = new User();
        inviter.setEmail("admin@qonnect.com");
        inviter.setRole(Role.ADMIN);
        inviter.setOrganization(org);

        String inviteeEmail = "invitee@example.com";

        when(userOutputPort.getUserByEmail(inviter.getEmail()))
                .thenReturn(inviter);
        when(userOutputPort.userExistsByEmail(inviteeEmail))
                .thenReturn(false);

        registrationService.inviteUser(inviter, inviteeEmail, Role.QA_ENGINEER);

        verify(userOutputPort).saveUser(argThat(u ->
                inviteeEmail.equals(u.getEmail())
                        && u.getRole() == Role.QA_ENGINEER
                        && u.isInvited()
                        && !u.isEnabled()
                        && u.getOrganization() == org
                        && u.getInviteToken() != null
        ));
        verify(emailOutputPort).sendEmail(eq(inviteeEmail), contains("You're Invited"), anyString());
    }

    @Test
    void shouldThrowWhenInviterIsNotAdmin() {
        User inviter = new User();
        inviter.setEmail("tester@qonnect.com");
        inviter.setRole(Role.QA_ENGINEER);

        when(userOutputPort.getUserByEmail(inviter.getEmail()))
                .thenReturn(inviter);

        assertThrows(AccessDeniedException.class,
                () -> registrationService.inviteUser(inviter,
                        "new@qonnect.com",
                        Role.QA_ENGINEER));

        verify(userOutputPort).getUserByEmail(inviter.getEmail());
        verifyNoMoreInteractions(userOutputPort);
        verifyNoInteractions(emailOutputPort);
    }


    @Test
    void shouldThrowWhenInviteeAlreadyExists() {
        User inviter = new User();
        inviter.setEmail("admin@qonnect.com");
        inviter.setRole(Role.ADMIN);
        inviter.setOrganization(org);

        when(userOutputPort.getUserByEmail(inviter.getEmail()))
                .thenReturn(inviter);
        when(userOutputPort.userExistsByEmail("dup@qonnect.com"))
                .thenReturn(true);

        assertThrows(UserAlreadyExistException.class,
                () -> registrationService.inviteUser(inviter, "dup@qonnect.com", Role.QA_ENGINEER));

        verify(emailOutputPort, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void shouldThrowWhenInviteeEmailIsInvalid(String invalidEmail) {
        User inviter = new User();
        inviter.setEmail("admin@qonnect.com");
        inviter.setRole(Role.ADMIN);
        inviter.setOrganization(org);

        when(userOutputPort.getUserByEmail(inviter.getEmail()))
                .thenReturn(inviter);

        assertThrows(IllegalArgumentException.class,
                () -> registrationService.inviteUser(inviter, invalidEmail, Role.QA_ENGINEER));
    }



    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }

    @Test
    void shouldRemoveUserFromOrganizationSuccessfully() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        User userToRemove = new User();
        userToRemove.setId(2L);

        Organization organization = new Organization();
        organization.setId(10L);
        organization.setUsers(List.of(admin, userToRemove));
        when(userOutputPort.existById(admin.getId())).thenReturn(true);

        when(organizationOutputPort.getOrganizationById(organization.getId()))
                .thenReturn(organization);
        when(userOutputPort.getUserById(userToRemove.getId()))
                .thenReturn(userToRemove);


        registrationService.removeUserFromAnOrganization(admin, userToRemove.getId(), organization.getId());

        verify(organizationOutputPort).removeUserFromOrganization(userToRemove,organization);
    }

    @Test
    void shouldThrowAccessDeniedIfNotAdmin() {
        User nonAdmin = new User();
        nonAdmin.setId(1L);
        nonAdmin.setRole(Role.QA_ENGINEER);

        when(userOutputPort.existById(nonAdmin.getId())).thenReturn(true);


        assertThrows(AccessDeniedException.class, () -> {
            registrationService.removeUserFromAnOrganization(nonAdmin, 2L, 10L);
        });
    }

    @Test
    void shouldNotRemoveIfUsersNotInSameOrganization() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        User outsider = new User();
        outsider.setId(3L);
        Organization organization = new Organization();
        organization.setId(10L);
        organization.setUsers(List.of(admin));
        when(userOutputPort.existById(admin.getId())).thenReturn(true);

        when(organizationOutputPort.getOrganizationById(organization.getId()))
                .thenReturn(organization);
        when(userOutputPort.getUserById(outsider.getId()))
                .thenReturn(outsider);

        registrationService.removeUserFromAnOrganization(admin, outsider.getId(), organization.getId());

        verify(organizationOutputPort, never()).removeUserFromOrganization(any(),any());
    }


    @Test
    void getAllUsersInOrganization_shouldReturnPageOfUsers_WhenUserIsAdminAndExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(List.of(user));

        user.setRole(Role.ADMIN);

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

        assertThrows(UserNotFoundException.class,
                () -> registrationService.getAllUsersInOrganization(user, org.getId(), pageable));

    }

    @Test
    void getAllUsersInOrganization_shouldThrow_WhenUserIsNotAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        user.setRole(Role.DEVELOPER);

        when(userOutputPort.existById(user.getId())).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> registrationService.getAllUsersInOrganization(user, org.getId(), pageable));

    }







}
