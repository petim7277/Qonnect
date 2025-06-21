package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.SignUpUseCase;
import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.OtpOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.models.OtpType;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.qonnect.domain.validators.InputValidator.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements SignUpUseCase {

    private final UserOutputPort userOutputPort;

    private final PasswordEncoder passwordEncoder;

    private final IdentityManagementOutputPort identityManagementOutputPort;
    private final OtpService otpService;



    @Override
    public User signUp(User user) throws UserAlreadyExistException, IdentityManagementException {
        validateEmail(user.getEmail());
        validateName(user.getFirstName(), "First name");
        validateName(user.getLastName(), "Last name");
        validatePassword(user.getPassword());
        validateRole(user.getRole().name());

        if (userOutputPort.userExistsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY, HttpStatus.CONFLICT);
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user = identityManagementOutputPort.createUser(user);
        log.info("Created new user in identity manager: {}", user);

        user.setEnabled(false);
        user = userOutputPort.saveUser(user);
        log.info("User saved to database: email={}, id={}", user.getEmail(), user.getId());

        otpService.createOtp(user.getFirstName(), user.getEmail(), OtpType.VERIFICATION);
        log.info("Sent OTP to user: {}", user.getEmail());

        return user;
    }


}
