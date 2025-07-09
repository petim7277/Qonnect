package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.*;
import com.example.qonnect.domain.exceptions.*;
import com.example.qonnect.domain.models.enums.Role;
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
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    private final ResendOtpUseCases resendOtpUseCases;
    private final ViewUserProfileUseCase viewUserProfileUseCase;
    private final LoginUseCase loginUseCase;
    private final AcceptInviteUseCase acceptInviteUseCase;

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


    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully", content = @Content(schema = @Schema(implementation = LoginUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(
            @RequestBody @Valid @Parameter(description = "User login credentials") LoginUserRequest loginUserRequest)
            throws UserNotFoundException, InvalidCredentialsException, AuthenticationException {

        log.info("Login request received for email: {}", loginUserRequest.getEmail());

        User user = userRestMapper.toUser(loginUserRequest);
        User authenticatedUser = loginUseCase.login(user);
        LoginUserResponse response = userRestMapper.toLoginResponse(authenticatedUser);

        log.info("Successful login for email: {}", authenticatedUser.getEmail());

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
    @PutMapping("/password/change")
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
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody LogoutRequest request
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");

        logoutUseCase.logout(user, request.getRefreshToken(), accessToken);

        return ResponseEntity.ok(
                new LogoutResponse("Logout successful", LocalDateTime.now())
        );
    }



    @Operation(summary = "Resend OTP", description = "Resends OTP for verification or password reset")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP resent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or email")
    })
    @PostMapping("/otp/resend")
    public ResponseEntity<OtpVerificationResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        resendOtpUseCases.resendOtp(request.getEmail(), request.getOtpType());

        OtpVerificationResponse response = new OtpVerificationResponse();
        response.setEmail(request.getEmail());
        response.setMessage("OTP resent successfully.");
        response.setVerified(false);
        response.setVerifiedAt(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "View user profile", description = "Retrieves the profile of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Keycloak")
    @GetMapping("/userProfile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal User user) {
        User profile = viewUserProfileUseCase.viewUserProfile(user.getEmail());
        return ResponseEntity.ok(userRestMapper.toUserProfileResponse(profile));
    }

    @Operation(summary = "Complete Invitation", description = "Allows an invited user to complete their profile setup")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or token"),
            @ApiResponse(responseCode = "404", description = "Invitation token not found or expired")
    })
    @PostMapping("/complete-invitation")
    public ResponseEntity<?> completeInvitation(
            @RequestParam @Parameter(description = "Invitation token sent to email") String token,
            @RequestBody @Valid @Parameter(description = "User details to complete invitation") CompleteInviteRequest request
    ) {
        acceptInviteUseCase.completeInvitation(token, request.getFirstName(), request.getLastName(), request.getPassword());
        return ResponseEntity.ok(new CompleteInviteResponse("Invitation details completed. Please verify OTP sent to your email."));
    }

    @Operation(summary = "Verify Invitation OTP", description = "Verifies the OTP for invited users and activates their account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP verified and account activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid OTP format or missing data"),
            @ApiResponse(responseCode = "404", description = "User not found or OTP expired")
    })
    @PostMapping("/verify-invitation")
    public ResponseEntity<?> verifyInvitation(
            @RequestBody @Valid @Parameter(description = "OTP verification for invited user") VerifyInviteOtpRequest request
    ) {
        acceptInviteUseCase.verifyOtpAndActivate(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(new VerifyInviteResponse("Account verified and activated successfully."));
    }





}