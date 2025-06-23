package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.ChangePasswordUseCase;
import com.example.qonnect.application.input.ResetPasswordUseCase;
import com.example.qonnect.application.input.SignUpUseCase;
import com.example.qonnect.application.input.VerifyOtpUseCase;
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

import static com.example.qonnect.domain.models.User.validateUserDetails;
import static com.example.qonnect.domain.validators.InputValidator.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements SignUpUseCase, VerifyOtpUseCase, ResetPasswordUseCase, ChangePasswordUseCase {

    private final UserOutputPort userOutputPort;

    private final PasswordEncoder passwordEncoder;

    private final IdentityManagementOutputPort identityManagementOutputPort;
    private final OtpService otpService;



    @Override
    public User signUp(User user) throws UserAlreadyExistException, IdentityManagementException {
        validateUserDetails(user);
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


    @Override
    public void verifyOtp(String email, String otp) {
        validateEmail(email);
        validateInput(otp);

        User user = userOutputPort.getUserByEmail(email);
        otpService.validateOtp(user.getEmail(), otp);

        user.setEnabled(true);
        userOutputPort.saveUser(user);

        log.info("OTP verified and user enabled: {}", user.getEmail());
    }

    @Override
    public void initiateReset(String email) {
        validateEmail(email);
        User user = userOutputPort.getUserByEmail(email);

        otpService.createOtp(user.getFirstName(), user.getEmail(), OtpType.RESET_PASSWORD);
        log.info("Reset password OTP sent to: {}", email);
    }

    @Override
    public void completeReset(String email, String otp, String newPassword) {
        validateEmail(email);
        validatePassword(newPassword);
        validateInput(otp);

        User user = userOutputPort.getUserByEmail(email);
        otpService.validateOtp(user.getEmail(), otp);

        user.setPassword(newPassword);
        identityManagementOutputPort.resetPassword(user);
        log.info("Password reset successful for user: {}", email);
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        validateEmail(email);
        validatePassword(oldPassword);
        validatePassword(newPassword);

        User user = userOutputPort.getUserByEmail(email);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IdentityManagementException(ErrorMessages.INCORRECT_OLD_PASSWORD, HttpStatus.UNAUTHORIZED);
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IdentityManagementException(ErrorMessages.NEW_PASSWORD_SAME_AS_OLD, HttpStatus.BAD_REQUEST);
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setNewPassword(encodedNewPassword);
        user.setPassword(encodedNewPassword);

        identityManagementOutputPort.changePassword(user);

        userOutputPort.saveUser(user);
    }



}
