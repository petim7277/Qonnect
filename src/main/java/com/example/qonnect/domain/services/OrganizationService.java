package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.InviteUserUseCase;
import com.example.qonnect.application.input.RegisterOrganizationAdminUseCase;
import com.example.qonnect.application.input.RemoveUserFromAnOrganizationUseCase;
import com.example.qonnect.application.input.ViewUserUserCase;
import com.example.qonnect.application.output.*;
import com.example.qonnect.domain.exceptions.OrganizationAlreadyExistsException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.enums.OtpType;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.template.EmailTemplate;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.qonnect.domain.validators.GeneralValidator.validateUserExists;
import static com.example.qonnect.domain.validators.GeneralValidator.validateUserIsAdmin;
import static com.example.qonnect.domain.validators.InputValidator.*;

@Service
@RequiredArgsConstructor
public class OrganizationService implements RegisterOrganizationAdminUseCase, InviteUserUseCase, RemoveUserFromAnOrganizationUseCase, ViewUserUserCase {

    private final UserOutputPort userOutputPort;
    private final OrganizationOutputPort organizationOutputPort;
    private final IdentityManagementOutputPort identityManagementOutputPort;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailOutputPort emailOutputPort;


    @Value("${base-url}")
    private String baseUrl;

    @Override
    public User registerOrganizationAdmin(User user, Organization organization) {

        validateName(user.getFirstName(), "first name");
        validateName(user.getLastName(), "last name");
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
        validateName(organization.getName(), "organization name");

        if (userOutputPort.userExistsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY, HttpStatus.CONFLICT);
        }

        if (organizationOutputPort.existsByName(organization.getName())) {
            throw new OrganizationAlreadyExistsException(ErrorMessages.ORGANIZATION_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Organization savedOrg = organizationOutputPort.saveOrganization(organization);

        user.setOrganization(savedOrg);
        user.setRole(Role.ADMIN);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);

        user = identityManagementOutputPort.createUser(user);
        user = userOutputPort.saveUser(user);
        otpService.createOtp(user.getFirstName(),user.getEmail(), OtpType.VERIFICATION);
        return user;
    }



    @Override
    public void inviteUser(User inviter, String inviteeEmail, Role roleToAssign) {
        User foundUser = userOutputPort.getUserByEmail(inviter.getEmail());
        if (foundUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
        validateEmail(inviteeEmail);
        validateRole(roleToAssign.name());

        if (userOutputPort.userExistsByEmail(inviteeEmail)) {
            throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY,HttpStatus.CONFLICT);
        }

        String token = generateInviteToken();
        LocalDateTime expiry = LocalDateTime.now().plusDays(7);

        User invitee = new User();
        invitee.setEmail(inviteeEmail);
        invitee.setRole(roleToAssign);
        invitee.setOrganization(foundUser.getOrganization());
        invitee.setInviteToken(token);
        invitee.setTokenExpiresAt(expiry);
        invitee.setInvited(true);
        invitee.setEnabled(false);

        userOutputPort.saveUser(invitee);
        sendInviteMail(invitee.getEmail(), token, foundUser);
    }


    private String generateInviteToken() {
        return UUID.randomUUID().toString();
    }

    private void sendInviteMail(String inviteeEmail, String token, User inviter) {
        String orgName = inviter.getOrganization().getName();
        String inviteLink = baseUrl + "/accept-invite?token=" + token;

        String subject = "You're Invited to Join " + orgName;

        String body = EmailTemplate.generateInviteBody(
                orgName,
                inviteLink
        );

        emailOutputPort.sendEmail(inviteeEmail, subject, body);
    }


    @Override
    public void removeUserFromAnOrganization(User user, Long userToBeRemovedId, Long organizationId) {
        validateUserExists(user);
        if (!userOutputPort.existById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        validateUserIsAdmin(user);

        Organization organization = organizationOutputPort.getOrganizationById(organizationId);
        User userToBeRemoved = userOutputPort.getUserById(userToBeRemovedId);
            if(organization.getUsers().stream().map(User::getId).anyMatch(user.getId()::equals) && organization.getUsers().stream().map(User::getId).anyMatch(userToBeRemoved.getId()::equals )){
                organizationOutputPort.removeUserFromOrganization(userToBeRemoved, organization);
            }
    }

    @Override
    public Page<User> getAllUsersInOrganization(User user,Long organizationId, Pageable pageable) {
        validateUserExists(user);
        if (!userOutputPort.existById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        validateUserIsAdmin(user);
        return userOutputPort.findAllByOrganizationId(organizationId, pageable);
    }
}

