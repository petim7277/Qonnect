package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.RegisterOrganizationAdminUseCase;
import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.OrganizationOutputPort;
import com.example.qonnect.application.output.OtpOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationAlreadyExistsException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.OtpType;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.qonnect.domain.validators.InputValidator.*;

@Service
@RequiredArgsConstructor
public class OrganizationService implements RegisterOrganizationAdminUseCase {

    private final UserOutputPort userOutputPort;
    private final OrganizationOutputPort organizationOutputPort;
    private final IdentityManagementOutputPort identityManagementOutputPort;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

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
}

