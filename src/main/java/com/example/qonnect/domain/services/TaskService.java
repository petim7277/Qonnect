package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.CreateTaskUseCase;
import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.TaskAlreadyExistException;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.enums.TaskStatus;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.example.qonnect.domain.validators.InputValidator.validateName;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService implements CreateTaskUseCase {

    private final TaskOutputPort taskOutputPort;
    private final UserOutputPort userOutputPort;
    private final ProjectOutputPort projectOutputPort;

    @Override
    public Task createTask(User user, Task task) {
        User foundUser = userOutputPort.getUserByEmail(user.getEmail());

        if (!foundUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }

        validateName(task.getTitle(), "Title");
        validateName(task.getDescription(), "Description");

        Project project = projectOutputPort.getProjectById(task.getProject().getId());

        if (taskOutputPort.existsByTitleAndProjectId(task.getTitle(), project.getId())) {
            throw new TaskAlreadyExistException(ErrorMessages.TASK_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        task.setProject(project);

        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskOutputPort.saveTask(task);
    }


}
