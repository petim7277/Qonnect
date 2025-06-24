package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.*;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.OtpException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.*;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.*;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operations related to user authentication and management")
public class UserController {

    private final SignUpUseCase signUpUseCase;
    private final UserRestMapper userRestMapper;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final LogoutUseCase logoutUseCase;


    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = RegisterUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "400", description = "fields are  empty"),
    })
    @PostMapping("/user")
    public ResponseEntity<RegisterUserResponse> registerUser(
            @RequestBody @Valid @Parameter(description = "User registration details") RegisterUserRequest registerRequest)
            throws UserNotFoundException, UserAlreadyExistException, IdentityManagementException {

        log.info("Registration request for email: {}", registerRequest.getEmail());

        User user = userRestMapper.toUser(registerRequest);
        user.setPassword(registerRequest.getPassword());
        user.setRole(Role.from(registerRequest.getRole()));
        User registeredUser = signUpUseCase.signUp(user);

        RegisterUserResponse response = userRestMapper.toCreateUserResponse(registeredUser);
        response.setMessage("Registration successful");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Verify user OTP", description = "Verifies the OTP sent to the user's email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP verified successfully",
                    content = @Content(schema = @Schema(implementation = OtpVerificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid OTP or input"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/otp")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(
            @Valid @RequestBody @Parameter(description = "OTP verification request") OtpVerificationRequest request)
            throws OtpException, UserNotFoundException {

        verifyOtpUseCase.verifyOtp(request.getEmail(), request.getOtp());

        OtpVerificationResponse response = new OtpVerificationResponse();
        response.setEmail(request.getEmail());
        response.setVerified(true);
        response.setMessage("OTP verified. Account activated successfully.");
        response.setVerifiedAt(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Initiate password reset", description = "Sends an OTP to the user's email for password reset")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP sent to email"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Keycloak")
    @PostMapping("/password/reset/initiate")
    public ResponseEntity<InitiateResetPasswordResponse> initiatePasswordReset(@AuthenticationPrincipal User user

    ) {
        resetPasswordUseCase.initiateReset(user.getEmail());

        return ResponseEntity.ok(new InitiateResetPasswordResponse("OTP sent to your email for password reset.",LocalDateTime.now()));
    }





    @Operation(summary = "Complete password reset", description = "Verifies OTP and sets new password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid OTP or password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Keycloak")
    @PostMapping("/password/reset/complete")
    public ResponseEntity<CompleteResetPasswordResponse> completePasswordReset(@AuthenticationPrincipal User user,
            @Valid @RequestBody CompleteResetPasswordRequest request
    ) {
        resetPasswordUseCase.completeReset(user.getEmail(), request.getOtp(), request.getNewPassword());

        return ResponseEntity.ok(new CompleteResetPasswordResponse("Password reset successful.",LocalDateTime.now()));
    }


    @Operation(summary = "Change password", description = "Allows an authenticated user to change their password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or new password same as old"),
            @ApiResponse(responseCode = "401", description = "Incorrect old password or unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Keycloak")
    @PostMapping("/password/change")
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        changePasswordUseCase.changePassword(user.getEmail(), request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(
                new ChangePasswordResponse("Password changed successfully.", LocalDateTime.now())
        );
    }


    @Operation(summary = "Logout user", description = "Invalidates refresh token and logs out the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    @SecurityRequirement(name = "Keycloak")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody LogoutRequest request
    ) {
        logoutUseCase.logout(user, request.getRefreshToken());

        return ResponseEntity.ok(
                new LogoutResponse("Logout successful", LocalDateTime.now())
        );
    }

}