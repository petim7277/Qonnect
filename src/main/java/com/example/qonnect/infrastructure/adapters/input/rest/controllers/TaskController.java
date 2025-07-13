package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.*;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateTaskRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.UpdateTaskRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.TaskResponse;

import com.example.qonnect.infrastructure.adapters.input.rest.mapper.TaskRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tasks", description = "Operations related to tasks management")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Keycloak")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final TaskRestMapper taskRestMapper;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final ViewAllTaskInAProjectUseCase viewAllTaskInAProjectUseCase;
    private final ViewATaskUseCase viewATaskUseCase;
    private final AssignTaskUseCase assignTaskUseCase;
    private final ViewAllUserTaskUseCase viewAllUserTaskUseCase;

    @Operation(summary = "Create Task", description = "Create a new task within the user's organization")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/task")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateTaskRequest request
    ) {
        Task taskToCreate = taskRestMapper.toTask(request);
        Task created = createTaskUseCase.createTask(user, taskToCreate);
        TaskResponse response = taskRestMapper.toTaskResponse(created);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete Task", description = "Delete a task by project and task ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/projects/{projectId}/tasks/{taskId}")
    public ResponseEntity<String> deleteTask(
            @AuthenticationPrincipal User user,
            @PathVariable("projectId") Long projectId,
            @PathVariable("taskId") Long taskId
    ) {
        deleteTaskUseCase.deleteTask(user, projectId, taskId);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @Operation(summary = "Update Task", description = "Update task details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody @Valid UpdateTaskRequest request
    ) {
        Task partialUpdate = taskRestMapper.toTask(request);
        Task updated = updateTaskUseCase.updateTask(user, projectId, taskId, partialUpdate);
        return ResponseEntity.ok(taskRestMapper.toTaskResponse(updated));
    }

    @Operation(summary = "Get All Tasks in Project", description = "View all tasks in a specific project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> viewAllTasks(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId
    ) {
        List<Task> tasks = viewAllTaskInAProjectUseCase.getAllTasksInProject(user, projectId);
        List<TaskResponse> responseList = tasks.stream()
                .map(taskRestMapper::toTaskResponse)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Get Task in Project", description = "View a specific task in a project by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/projects/{projectId}/tasks/{taskId}")
    public ResponseEntity<TaskResponse> viewTaskInProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        Task task = viewATaskUseCase.viewTaskInProject(user, projectId, taskId);
        TaskResponse response = taskRestMapper.toTaskResponse(task);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assign Task to User", description = "Assign a task to a specific user (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Task or User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/tasks/{taskId}/assign/{userId}")
    public ResponseEntity<String> assignTaskToUser(
            @AuthenticationPrincipal User admin,
            @PathVariable Long taskId,
            @PathVariable Long userId
    ) {
        assignTaskUseCase.assignTaskToUser(admin, taskId, userId);
        return ResponseEntity.ok("Task assigned successfully");
    }

    @Operation(summary = "Self Assign Task", description = "Pick up a task by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task picked successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/tasks/{taskId}/self-assign")
    public ResponseEntity<String> pickTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long taskId
    ) {
        assignTaskUseCase.selfAssignTask(user, taskId);
        return ResponseEntity.ok("Task picked successfully");
    }

    @Operation(summary = "Get Tasks by User", description = "Retrieve all tasks assigned to a user with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<TaskResponse>> getTasksByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0) page = 0;
        if (size < 0) size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = viewAllUserTaskUseCase.getTasksByUserId(userId, pageable);

        List<TaskResponse> responseList = tasks.getContent().stream()
                .map(taskRestMapper::toTaskResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }
}







