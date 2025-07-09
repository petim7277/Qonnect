package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.BugUseCase;
import com.example.qonnect.application.output.*;
import com.example.qonnect.domain.exceptions.BugNotFoundException;
import com.example.qonnect.domain.exceptions.QonnectException;
import com.example.qonnect.domain.exceptions.TaskNotFoundException;
import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.BugStatus;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.validators.GeneralValidator;
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

import static com.example.qonnect.domain.validators.GeneralValidator.*;
import static com.example.qonnect.domain.validators.InputValidator.validateInput;

@Service
@RequiredArgsConstructor
@Slf4j
public class BugService implements BugUseCase {
    private final BugOutputPort bugOutputPort;
    private final TaskOutputPort taskOutputPort;
    private final ProjectOutputPort projectOutputPort;
    private final UserOutputPort userOutputPort;


    @Override
    @Transactional(readOnly = true)
    public Bug getBugById(User user, Long taskId, Long id) {
        log.info("Getting bug with ID: {} for task ID: {} by user: {}", id, taskId, user.getId());

        validateUserExists(user);
        validateBugId(id);
        validateTaskId(taskId);
        Task task = taskOutputPort.getTaskById(taskId);
        Project project = projectOutputPort.getProjectById(task.getProjectId());
        validateUserBelongsToProjectOrganization(user, project);
        Bug bug = bugOutputPort.getBugByIdAndTaskId(id, task.getId());

        if (bug == null) {
            throw new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        log.info("Successfully retrieved bug: {} for user: {}", bug.getId(), user.getId());
        return bug;
    }

    @Override
    @Transactional
    public Bug updateBugDetails(User user, Long taskId, Bug bug) {
        log.info("Updating bug details for bug ID: {} in task ID: {} by user: {}",
                bug.getId(), taskId, user.getId());

        validateUserExists(user);
        validateTaskId(taskId);


        if (bug.getDescription() == null) {
            throw new QonnectException(ErrorMessages.BUG_SEVERITY_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Task task = taskOutputPort.getTaskById(taskId);

        Project project = projectOutputPort.getProjectById(task.getProjectId());
        validateUserBelongsToProjectOrganization(user, project);
        Bug existingBug = bugOutputPort.getBugByIdAndTaskId(bug.getId(), taskId);
        if (existingBug == null) {
            throw new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }


        if (bug.getTitle() != null && !bug.getTitle().isEmpty()) {
            existingBug.setTitle(bug.getTitle());
        }

        if (bug.getDescription() != null && !bug.getDescription().isEmpty()) {
            existingBug.setDescription(bug.getDescription());
        }

        existingBug.setUpdatedAt(LocalDateTime.now());
        Bug updatedBug = bugOutputPort.saveBug(existingBug);
        log.info("Successfully updated bug details for bug ID: {} by user: {}",
                bug.getId(), user.getId());
        return updatedBug;
    }

    @Override
    @Transactional
    public Bug updateBugStatus(User user, Long taskId, Bug bug) {
        log.info("Updating bug status for bug ID: {} in task ID: {} by user: {}",
                bug.getId(), taskId, user.getId());

        validateUserExists(user);
        validateTaskId(taskId);
        validateBugId(bug.getId());

        if (bug.getStatus() == null) {
            throw new QonnectException(ErrorMessages.BUG_STATUS_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }


        Task task = taskOutputPort.getTaskById(taskId);

        Project project = projectOutputPort.getProjectById(task.getProjectId());

        validateUserBelongsToProjectOrganization(user, project);

        Bug existingBug = bugOutputPort.getBugByIdAndTaskId(bug.getId(), taskId);
        if (existingBug == null) {
            throw new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        existingBug.setStatus(bug.getStatus());
        existingBug.setUpdatedAt(LocalDateTime.now());

        Bug updatedBug = bugOutputPort.saveBug(existingBug);

        log.info("Successfully updated bug status for bug ID: {} to status: {} by user: {}",
                bug.getId(), bug.getStatus(), user.getId());
        return updatedBug;
    }

    @Override
    @Transactional
    public Bug updateBugSeverity(User user, Long taskId, Bug bug) {
        log.info("Updating bug severity for bug ID: {} in task ID: {} by user: {}",
                bug.getId(), taskId, user.getId());

        validateUserExists(user);
        validateTaskId(taskId);
        validateBugId(bug.getId());

        if (bug.getSeverity() == null) {
            throw new QonnectException(ErrorMessages.BUG_SEVERITY_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Task task = taskOutputPort.getTaskById(taskId);

        Project project = projectOutputPort.getProjectById(task.getProjectId());

        validateUserBelongsToProjectOrganization(user, project);

        Bug existingBug = bugOutputPort.getBugByIdAndTaskId(bug.getId(), taskId);
        if (existingBug == null) {
            throw new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        existingBug.setSeverity(bug.getSeverity());
        existingBug.setUpdatedAt(LocalDateTime.now());
        Bug updatedBug = bugOutputPort.saveBug(existingBug);

        log.info("Successfully updated bug severity for bug ID: {} to status: {} by user: {}",
                bug.getId(), bug.getSeverity(), user.getId());
        return updatedBug;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getAllBugsInAProject(User user, Long projectId, Pageable pageable) {
        log.info("Getting all bugs in project ID: {} by user: {} with pagination: {}",
                projectId, user.getId(), pageable);
        validateUserExists(user);
        GeneralValidator.validateProjectId(projectId);
        Project project = projectOutputPort.getProjectById(projectId);
        validateUserBelongsToProjectOrganization(user, project);
        Page<Bug> bugs = bugOutputPort.getAllBugsByProjectId(projectId, pageable);

        log.info("Successfully retrieved {} bugs in project ID: {} for user: {}",
                bugs.getTotalElements(), projectId, user.getId());
        return bugs;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getAllBugsInATask(User user, Long taskId, Pageable pageable) {
        log.info("Getting all bugs in task ID: {} by user: {} with pagination: {}",
                taskId, user.getId(), pageable);

        validateUserExists(user);
        validateTaskId(taskId);
        Task task = taskOutputPort.getTaskById(taskId);
        Project project = projectOutputPort.getProjectById(task.getProjectId());
        validateUserBelongsToProjectOrganization(user, project);
        Page<Bug> bugs = bugOutputPort.getAllBugsByTaskId(taskId, pageable);
        log.info("Successfully retrieved {} bugs in task ID: {} for user: {}",
                bugs.getTotalElements(), taskId, user.getId());
        return bugs;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getBugsByUserId(Long userId, Pageable pageable) {
        log.info("Getting bugs for user ID: {} with pagination: {}", userId, pageable);
        if (userId == null) {
            throw new QonnectException(ErrorMessages.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        Page<Bug> bugs = bugOutputPort.getBugsByUserId(userId, pageable);
        log.info("Successfully retrieved {} bugs for user ID: {}",
                bugs.getTotalElements(), userId);
        return bugs;
    }


    public Bug reportBug(User reporter, Bug bug) {
        Project project = projectOutputPort.getProjectById(bug.getProjectId());

        if (!belongsToSameOrganization(reporter, project)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_TO_ORGANIZATION);
        }

        if (!isQaEngineer(reporter)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_TO_REPORT_BUG);
        }

        if (bug.getTaskId() != null) {
            Task task = taskOutputPort.getTaskById(bug.getTaskId());
            System.out.println("Fetched task = " + task);
            System.out.println("task.getProjectId() = " + task.getProjectId());

            if (!task.getProjectId().equals(bug.getProjectId())) {
                throw new IllegalArgumentException("Task does not belong to the specified project");
            }
        }

        validateInput(bug.getTitle());
        validateInput(bug.getDescription());
        validateInput(bug.getDescription());

        if (bugOutputPort.existsByTitleAndProjectId(bug.getTitle(), bug.getProjectId())) {
            throw new IllegalArgumentException("Bug with this title already exists in the project");
        }


        bug.setCreatedBy(reporter);
        bug.setStatus(BugStatus.OPEN);

        return bugOutputPort.saveBug(bug);
    }

    @Override
    public Bug assignBugToDeveloper(User assigner, Long bugId, Long developerId) {
        Bug bug = bugOutputPort.getBugById(bugId);
        User developer = userOutputPort.getUserById(developerId);

        validateUserBelongsOrganization(assigner, bug.getCreatedBy().getOrganization().getId());
        validateUserBelongsOrganization(developer, bug.getCreatedBy().getOrganization().getId());

        if (!(assigner.getRole() == Role.ADMIN || assigner.getRole() == Role.QA_ENGINEER)) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_TO_ASSIGN_BUG);
        }


        if (developer.getRole() != Role.DEVELOPER) {
            throw new IllegalArgumentException("Assigned user must be a developer");
        }

        if(bug.getAssignedTo()!=null&& bug.getAssignedTo().getId().equals(developer.getId())) {
            throw new IllegalArgumentException("Assigned user is already assigned to this bug");
        }

        bug.setAssignedTo(developer);
        return bugOutputPort.saveBug(bug);
    }


    private boolean belongsToSameOrganization(User user, Project project) {
        return user.getOrganization().getId().equals(project.getOrganizationId());
    }

    private boolean isQaEngineer(User user) {
        return Role.QA_ENGINEER.equals(user.getRole());
    }


    private void validateBugId(Long bugId) {
        if (bugId == null) {
            throw new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateTaskId(Long taskId) {
        if (taskId == null) {
            throw new TaskNotFoundException(ErrorMessages.TASK_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
    }
}

