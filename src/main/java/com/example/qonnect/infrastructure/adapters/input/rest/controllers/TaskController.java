package com.example.qonnect.infrastructure.adapters.input.rest.controllers;

import com.example.qonnect.application.input.CreateTaskUseCase;
import com.example.qonnect.application.input.DeleteTaskUseCase;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateTaskRequest;
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

}
