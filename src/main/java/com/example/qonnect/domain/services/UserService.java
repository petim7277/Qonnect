package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.*;
import com.example.qonnect.application.output.IdentityManagementOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.AuthenticationException;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.OtpType;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.config.security.TokenBlacklistService;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import static com.example.qonnect.domain.models.User.validateUserDetails;
import static com.example.qonnect.domain.validators.InputValidator.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements SignUpUseCase, VerifyOtpUseCase, ResetPasswordUseCase, ChangePasswordUseCase, LogoutUseCase, ResendOtpUseCases, ViewUserProfileUseCase, LoginUseCase{

    private final UserOutputPort userOutputPort;
    private final PasswordEncoder passwordEncoder;
    private final IdentityManagementOutputPort identityManagementOutputPort;
    private final OtpService otpService;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistService tokenBlacklistService;




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
        user.setVerified(true);
        userOutputPort.saveUser(user);

        log.info("OTP verified and user enabled: {}", user.getEmail());
    }


    @Override
    public User login(User user) throws UserNotFoundException, AuthenticationException, InvalidCredentialsException {
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());

        User foundUser = userOutputPort.getUserByEmail(user.getEmail());
        if (foundUser == null) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND,HttpStatus.NOT_FOUND);
        }

        log.info("found user password {}", foundUser.getPassword());

        if (!passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            throw new InvalidCredentialsException(ErrorMessages.INVALID_CREDENTIALS);
        }

        if (!foundUser.isEnabled()) {
            throw new IllegalStateException(ErrorMessages.USER_NOT_ENABLED);
        }

        log.info("found user enabled {}", foundUser);

        identityManagementOutputPort.login(foundUser);

        log.info("User with email {} has logged in successfully", user.getEmail());
        return foundUser;
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

        User user = userOutputPort.getUserByEmail(email);
        otpService.validateOtp(email, otp);

        String encoded = passwordEncoder.encode(newPassword);

        log.info("✅ Encoded password during reset: {}", encoded); // 👈 ADD THIS LINE

        user.setPassword(encoded);
        user.setNewPassword(encoded);

        identityManagementOutputPort.resetPassword(user);
        userOutputPort.saveUser(user);
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


    @Override
    public void logout(User user, String refreshToken, String accessToken) {
        identityManagementOutputPort.logout(user, refreshToken);

        Jwt jwt = jwtDecoder.decode(accessToken);

        String jti = jwt.getClaimAsString("jti");
        Instant expiry = jwt.getExpiresAt();
        Instant now = Instant.now();

        if (jti != null && expiry != null) {
            long ttlSeconds = Duration.between(now, expiry).getSeconds();
            log.info("📝 Storing jti={} in Redis with TTL={} seconds", jti, ttlSeconds); // 👈 ADD THIS
            tokenBlacklistService.blacklistToken(jti, ttlSeconds);
            log.info("🔒 Token with jti={} blacklisted for {} seconds", jti, ttlSeconds);
        } else {
            log.warn("⚠️ Could not extract jti or expiry from token during logout");
        }
    }


    @Override
    public void resendOtp(String email, OtpType otpType) {
        validateEmail(email);
        User foundUser = userOutputPort.getUserByEmail(email);
        otpService.resendOtp(foundUser.getFirstName(), foundUser.getEmail(), otpType);
    }

    @Override
    public User viewUserProfile(String email) {
        validateEmail(email);
        return userOutputPort.getUserByEmail(email);
    }

}
