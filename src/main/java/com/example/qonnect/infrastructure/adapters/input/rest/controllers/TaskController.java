package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.CreateTaskUseCase;
import com.example.qonnect.application.input.DeleteTaskUseCase;
import com.example.qonnect.application.input.UpdateTaskUseCase;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateTaskRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.UpdateTaskRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.TaskResponse;

import com.example.qonnect.infrastructure.adapters.input.rest.mapper.TaskRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {


    private final CreateTaskUseCase createTaskUseCase;
    private final TaskRestMapper taskRestMapper;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;

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


    @DeleteMapping("/projects/{projectId}/tasks/{taskId}")
    public ResponseEntity<String> deleteTask(
            @AuthenticationPrincipal User user,
            @PathVariable ("projectId") Long projectId,
            @PathVariable ("taskId") Long taskId
    ) {
        deleteTaskUseCase.deleteTask(user, projectId, taskId);
        return ResponseEntity.ok("Task deleted successfully");
    }


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



}
