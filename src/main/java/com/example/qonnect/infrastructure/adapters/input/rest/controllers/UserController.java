package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.SignUpUseCase;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.RegisterUserRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.RegisterUserResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operations related to user authentication and management")
public class UserController {

    private final SignUpUseCase signUpUseCase;
    private final UserRestMapper userRestMapper;


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
}