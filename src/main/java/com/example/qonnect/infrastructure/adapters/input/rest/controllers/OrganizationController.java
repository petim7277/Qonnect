package com.example.qonnect.infrastructure.adapters.input.rest.controllers;


import com.example.qonnect.application.input.InviteUserUseCase;
import com.example.qonnect.application.input.RegisterOrganizationAdminUseCase;
import com.example.qonnect.application.input.RemoveUserFromAnOrganizationUseCase;
import com.example.qonnect.application.input.ViewUserUserCase;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.OrganizationAlreadyExistsException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.InviteUserRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.RegisterOrganizationAdminRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.RegisterOrganizationAdminResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.UserResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.OrganizationRestMapper;
import com.example.qonnect.infrastructure.adapters.output.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final RegisterOrganizationAdminUseCase registerOrganizationAdminUseCase;

    private final OrganizationRestMapper organizationRestMapper;
    private final InviteUserUseCase inviteUserUseCase;

    private final RemoveUserFromAnOrganizationUseCase removeUserFromAnOrganizationUserCase;
    private final ViewUserUserCase viewUserUserCase;
    private  final UserMapper userMapper;


    @PostMapping("/organization")
    public ResponseEntity<RegisterOrganizationAdminResponse> registerOrganization(
            @Valid @RequestBody RegisterOrganizationAdminRequest request)
            throws UserAlreadyExistException, OrganizationAlreadyExistsException, IdentityManagementException {

        Organization org = organizationRestMapper.toOrganization(request);
        User user = organizationRestMapper.toUser(request);
        user.setPassword(request.getPassword());

        User registeredAdmin = registerOrganizationAdminUseCase.registerOrganizationAdmin(user, org);

        RegisterOrganizationAdminResponse response = organizationRestMapper.toRegisterResponse(registeredAdmin, org);
        response.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/invite")
    public ResponseEntity<String> inviteUser(
            @AuthenticationPrincipal User inviter,
            @Valid @RequestBody InviteUserRequest request
    ) {
        inviteUserUseCase.inviteUser(inviter, request.getEmail(), request.getRole());
        return ResponseEntity.ok("Invitation sent successfully.");
    }

    @DeleteMapping("/{organizationId}/users/{userToBeRemovedId}")
    public ResponseEntity<String> removeUserFromAnOrganization(
            @AuthenticationPrincipal User user,
            @PathVariable Long organizationId,
            @PathVariable Long userToBeRemovedId) {

        removeUserFromAnOrganizationUserCase.removeUserFromAnOrganization(user, userToBeRemovedId, organizationId);
        return ResponseEntity.ok("User removed successfully.");
    }


    @GetMapping("/{organizationId}/users")
    public ResponseEntity<Page<UserResponse>> getAllUsersInOrganization(
            @AuthenticationPrincipal User user,
            @PathVariable Long organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<User> users= viewUserUserCase.getAllUsersInOrganization(user, organizationId, pageable);
        Page<UserResponse> userResponse = users.map(userMapper::toUserResponse);
        return ResponseEntity.ok(userResponse);
    }


}
