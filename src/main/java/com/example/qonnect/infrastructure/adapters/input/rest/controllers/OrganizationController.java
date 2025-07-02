package com.example.qonnect.infrastructure.adapters.input.rest.controllers;


import com.example.qonnect.application.input.InviteUserUseCase;
import com.example.qonnect.application.input.RegisterOrganizationAdminUseCase;
import com.example.qonnect.domain.exceptions.IdentityManagementException;
import com.example.qonnect.domain.exceptions.OrganizationAlreadyExistsException;
import com.example.qonnect.domain.exceptions.UserAlreadyExistException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.InviteUserRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.RegisterOrganizationAdminRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.RegisterOrganizationAdminResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.OrganizationRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final RegisterOrganizationAdminUseCase registerOrganizationAdminUseCase;

    private final OrganizationRestMapper organizationRestMapper;
    private final InviteUserUseCase inviteUserUseCase;


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




}
