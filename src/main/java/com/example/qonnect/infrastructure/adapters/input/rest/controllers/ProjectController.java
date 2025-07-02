package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.ProjectUseCase;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.ProjectCreationResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.ProjectRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
