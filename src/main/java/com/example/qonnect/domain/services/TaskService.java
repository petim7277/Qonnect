package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.*;
import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.QonnectException;
import com.example.qonnect.domain.exceptions.TaskAlreadyAssignedException;
import com.example.qonnect.domain.exceptions.TaskAlreadyExistException;
import com.example.qonnect.domain.exceptions.TaskNotFoundException;
import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.enums.TaskStatus;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.qonnect.domain.validators.GeneralValidator.validateUserBelongsOrganization;
import static com.example.qonnect.domain.validators.GeneralValidator.validateUserIsAdmin;
import static com.example.qonnect.domain.validators.InputValidator.validateName;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService implements CreateTaskUseCase, DeleteTaskUseCase, UpdateTaskUseCase, ViewAllTaskInAProjectUseCase, ViewATaskUseCase,AssignTaskUseCase, ViewAllUserTaskUseCase {

    private final TaskOutputPort taskOutputPort;
    private final UserOutputPort userOutputPort;
    private final ProjectOutputPort projectOutputPort;

    @Override
    public Task createTask(User user, Task task) {
        User foundUser = userOutputPort.getUserByEmail(user.getEmail());

        validateUserIsAdmin(foundUser);

        validateName(task.getTitle(), "Title");
        validateName(task.getDescription(), "Description");

        Project project = projectOutputPort.getProjectById(task.getProjectId());
        validateUserBelongsOrganization(user,project.getOrganizationId());

        if (taskOutputPort.existsByTitleAndProjectId(task.getTitle(), project.getId())) {
            throw new TaskAlreadyExistException(ErrorMessages.TASK_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        task.setProjectId(project.getId());

        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskOutputPort.saveTask(task);
    }


    @Override
    public void deleteTask(User user, Long projectId, Long taskId) {
        User foundUser = userOutputPort.getUserByEmail(user.getEmail());
        validateUserIsAdmin(foundUser);
        Project project = projectOutputPort.getProjectById(projectId);
        validateUserBelongsOrganization(user,project.getOrganizationId());

        Task task = taskOutputPort.getTaskById(taskId);
        if (!task.getProjectId().equals(project.getId())) {
            throw new TaskNotFoundException(ErrorMessages.TASK_NOT_FOUND_IN_PROJECT, HttpStatus.NOT_FOUND);
        }

        taskOutputPort.deleteTaskById(taskId);
    }


    @Override
    public Task updateTask(User user, Long projectId, Long taskId, Task updatedTask) {
        User foundUser = userOutputPort.getUserByEmail(user.getEmail());
        validateUserIsAdmin(foundUser);
        Project project = projectOutputPort.getProjectById(projectId);
        validateUserBelongsOrganization(user,project.getOrganizationId());
        Task existing = taskOutputPort.getTaskById(taskId);
        if (!existing.getProjectId().equals(project.getId())) {
            throw new TaskNotFoundException(ErrorMessages.TASK_NOT_FOUND_IN_PROJECT, HttpStatus.NOT_FOUND);
        }

        validateName(updatedTask.getTitle(), "Title");
        validateName(updatedTask.getDescription(), "Description");

        if (updatedTask.getDueDate() != null && updatedTask.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(ErrorMessages.DUE_DATE_INVALID);
        }

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setUpdatedAt(LocalDateTime.now());

        return taskOutputPort.saveTask(existing);
    }

    @Override
    public List<Task> getAllTasksInProject(User user, Long projectId) {
        Project project = projectOutputPort.getProjectById(projectId);
        validateUserBelongsOrganization(user,project.getOrganizationId());
        return taskOutputPort.getAllTasksByProjectId(projectId);
    }

    @Override
    public Task viewTaskInProject(User user, Long projectId, Long taskId) {
        Project project = projectOutputPort.getProjectById(projectId);
        validateUserBelongsOrganization(user,project.getOrganizationId());
        boolean isMember = project.getTeamMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId())) || user.getRole().equals(Role.ADMIN);


        if (!isMember) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_TO_VIEW_TASK);
        }

        Task task = taskOutputPort.getTaskById(taskId);
        if (!task.getProjectId().equals(projectId)) {
            throw new TaskNotFoundException(ErrorMessages.TASK_NOT_FOUND_IN_PROJECT, HttpStatus.NOT_FOUND);
        }

        return task;
    }

    @Override
    public Task assignTaskToUser(User admin, Long taskId, Long userId) {
        User foundAdmin = userOutputPort.getUserByEmail(admin.getEmail());

        if (!foundAdmin.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_TO_ASSIGN_TASK);
        }

        Task task = taskOutputPort.getTaskById(taskId);
        Project project = projectOutputPort.getProjectById(task.getProjectId());

        validateUserBelongsOrganization(foundAdmin, project.getOrganizationId());

        User developer = userOutputPort.getUserById(userId);

        if (!developer.getRole().equals(Role.DEVELOPER)) {
            throw new AccessDeniedException(ErrorMessages.ONLY_DEVELOPER_CAN_BE_ASSIGNED_TASK);
        }

        validateUserBelongsOrganization(developer, project.getOrganizationId());

        task.setAssignedTo(developer);
        task.setUpdatedAt(LocalDateTime.now());

        return taskOutputPort.saveTask(task);
    }

    @Override
    public Task selfAssignTask(User user, Long taskId) {
        User foundUser = userOutputPort.getUserByEmail(user.getEmail());

        if (!foundUser.getRole().equals(Role.DEVELOPER)) {
            throw new AccessDeniedException(ErrorMessages.ONLY_DEVELOPER_CAN_PICK_TASK);
        }

        Task task = taskOutputPort.getTaskById(taskId);
        Project project = projectOutputPort.getProjectById(task.getProjectId());

        validateUserBelongsOrganization(foundUser, project.getOrganizationId());

        if (task.getAssignedTo() != null) {
            throw new TaskAlreadyAssignedException(ErrorMessages.TASK_ALREADY_ASSIGNED, HttpStatus.CONFLICT);
        }

        task.setAssignedTo(foundUser);
        task.setUpdatedAt(LocalDateTime.now());

        return taskOutputPort.saveTask(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> getTasksByUserId(Long userId, Pageable pageable) {
        log.info("Getting bugs for user ID: {} with pagination: {}", userId, pageable);
        if (userId == null) {
            throw new QonnectException(ErrorMessages.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        Page<Task> tasks = taskOutputPort.getTasksByUserId(userId, pageable);
        log.info("Successfully retrieved {} bugs for user ID: {}",
                tasks.getTotalElements(), userId);
        return tasks;
    }

}
