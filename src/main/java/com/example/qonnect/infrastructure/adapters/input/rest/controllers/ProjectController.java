package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.AssignUserToProjectUseCase;
import com.example.qonnect.application.input.ProjectUseCase;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.ProjectCreationResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.ProjectResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.ProjectRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
@SecurityRequirement(name = "Keycloak")
public class ProjectController {

    private final ProjectUseCase projectUseCase;
    private final ProjectRestMapper projectRestMapper;
    private final AssignUserToProjectUseCase assignUserToProjectUseCase;

    @Operation(summary = "Create Project", description = "Allows an admin to create a new project in their organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (not an admin)"),
            @ApiResponse(responseCode = "409", description = "Project with name already exists")
    })
    @PostMapping("/project")
    public ResponseEntity<ProjectCreationResponse> createProject(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateProjectRequest request
    ) {
        Project project = projectRestMapper.toDomain(request);

        Project created = projectUseCase.createProject(user, project);
        return ResponseEntity.ok(projectRestMapper.toResponse(created));
    }


    @GetMapping()
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0",name = "page") int page,
            @RequestParam(defaultValue = "10",name = "size") int size
            ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projects = projectUseCase.getAllProjects(user.getOrganization().getId(), pageable);
        Page<ProjectResponse> responses = projects.map(projectRestMapper::toProjectResponse);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Assign user to project", description = "Assigns a user to a specific project. Only admins can perform this action.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User assigned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (not an admin)"),
            @ApiResponse(responseCode = "404", description = "Project or User not found"),
            @ApiResponse(responseCode = "409", description = "User already assigned to project")
    })
    @PostMapping("/{projectId}/assign")
    public ResponseEntity<Void> assignUserToProject(
            @AuthenticationPrincipal User performingUser,
            @PathVariable Long projectId,
            @RequestParam Long userId
    ) {
        assignUserToProjectUseCase.assignUserToProject(projectId, userId, performingUser);
        return ResponseEntity.ok().build();
    }

}
