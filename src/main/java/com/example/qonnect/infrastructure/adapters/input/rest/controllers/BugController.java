package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.BugUseCase;
import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.AssignBugResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.BugRestMapper;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.UpdateBugRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.BugResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateBugRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bugs")
@SecurityRequirement(name = "Keycloak")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Bugs", description = "Operations related to bug tracking and assignment")
public class BugController {

    private final BugUseCase bugUseCase;
    private final BugRestMapper bugRestMapper;

    @Operation(summary = "Get Bug by ID", description = "Get a bug using its ID and the task it belongs to.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bug retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Bug not found")
    })
    @GetMapping("/task/{taskId}/bug/{bugId}")
    public ResponseEntity<BugResponse> getBugById(
            @AuthenticationPrincipal User user,
            @PathVariable Long taskId,
            @PathVariable Long bugId
    ) {
        Bug bug = bugUseCase.getBugById(user, taskId, bugId);
        return ResponseEntity.ok(bugRestMapper.toResponse(bug));
    }

    @Operation(summary = "Update Bug Details", description = "Update the title or description of a bug.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bug updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    @PatchMapping("/task/{taskId}/details")
    public ResponseEntity<BugResponse> updateBugDetails(
            @AuthenticationPrincipal User user,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateBugRequest request
    ) {
        Bug bug = bugRestMapper.toDomain(request);
        Bug updated = bugUseCase.updateBugDetails(user, taskId, bug);
        return ResponseEntity.ok(bugRestMapper.toResponse(updated));
    }

    @Operation(summary = "Update Bug Status", description = "Update the status of a bug.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bug status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    @PatchMapping("/task/{taskId}/status")
    public ResponseEntity<BugResponse> updateBugStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateBugRequest request
    ) {
        Bug bug = bugRestMapper.toDomain(request);
        Bug updated = bugUseCase.updateBugStatus(user, taskId, bug);
        return ResponseEntity.ok(bugRestMapper.toResponse(updated));
    }

    @Operation(summary = "Update Bug Severity", description = "Update the severity of a bug.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bug severity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    @PatchMapping("/task/{taskId}/severity")
    public ResponseEntity<BugResponse> updateBugSeverity(
            @AuthenticationPrincipal User user,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateBugRequest request
    ) {
        Bug bug = bugRestMapper.toDomain(request);
        Bug updated = bugUseCase.updateBugSeverity(user, taskId, bug);
        return ResponseEntity.ok(bugRestMapper.toResponse(updated));
    }

    @Operation(summary = "Get All Bugs in a Project", description = "Retrieve paginated bugs from a project.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bugs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<BugResponse>> getAllBugsInProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bug> bugs = bugUseCase.getAllBugsInAProject(user, projectId, pageable);
        return ResponseEntity.ok(bugs.map(bugRestMapper::toResponse));
    }

    @Operation(summary = "Get All Bugs in a Task", description = "Retrieve paginated bugs from a specific task.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bugs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<BugResponse>> getAllBugsInTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bug> bugs = bugUseCase.getAllBugsInATask(user, taskId, pageable);
        return ResponseEntity.ok(bugs.map(bugRestMapper::toResponse));
    }

    @Operation(summary = "Get Bugs assigned to a user ", description = "Retrieve paginated bugs created by a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bugs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BugResponse>> getBugsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bug> bugs = bugUseCase.getBugsByAssignedToId(userId, pageable);
        return ResponseEntity.ok(bugs.map(bugRestMapper::toResponse));
    }

    @Operation(summary = "Get Bugs created by a User", description = "Retrieve paginated bugs created by a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bugs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })

    @GetMapping("/user/creator/{userId}")
    public ResponseEntity<Page<BugResponse>> getBugsByCreatorId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bug> bugs = bugUseCase.getBugsByCreatedById(userId, pageable);
        return ResponseEntity.ok(bugs.map(bugRestMapper::toResponse));
    }

    @Operation(summary = "Report a new bug", description = "Allows a QA Engineer or user to report a bug in a project.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bug reported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{projectId}/bug")
    public ResponseEntity<BugResponse> reportBug(
            @PathVariable Long projectId,
            @RequestBody @Valid CreateBugRequest request,
            @AuthenticationPrincipal User user
    ) {
        Bug domainBug = bugRestMapper.toDomain(request);
        domainBug.setProjectId(projectId);
        Bug saved = bugUseCase.reportBug(user, domainBug);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bugRestMapper.toResponse(saved));
    }

    @Operation(summary = "Assign Bug to Developer", description = "Allows an admin to assign a reported bug to a developer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bug assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Bug or developer not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized assignment attempt")
    })
    @PostMapping("/{bugId}/assign/{developerId}")
    public ResponseEntity<AssignBugResponse> assignBugToDeveloper(
            @AuthenticationPrincipal User user,
            @PathVariable Long bugId,
            @PathVariable Long developerId) {

        Bug assignedBug = bugUseCase.assignBugToDeveloper(user, bugId, developerId);
        AssignBugResponse response = bugRestMapper.toAssignBugResponse(assignedBug);
        return ResponseEntity.ok(response);
    }
}
