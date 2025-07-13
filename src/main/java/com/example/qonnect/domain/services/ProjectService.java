package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.AssignUserToProjectUseCase;
import com.example.qonnect.application.input.ProjectUseCase;
import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.exceptions.ProjectAlreadyExistException;
import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
import com.example.qonnect.domain.exceptions.ProjectException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.qonnect.domain.validators.GeneralValidator.validateUserExists;
import static com.example.qonnect.domain.validators.GeneralValidator.validateUserIsAdmin;
import static com.example.qonnect.domain.validators.InputValidator.validateName;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService implements ProjectUseCase, AssignUserToProjectUseCase {

    private final ProjectOutputPort projectOutputPort;
    private final UserOutputPort userOutputPort;

    @Override
    public Project createProject(User user, Project project) {
        validateUserExists(user);
        validateUserIsAdmin(user);

        log.info("Here is the user organization id " + user.getOrganization());

        validateName(project.getName(), "project name");
        validateName(project.getDescription(), "project description");

        if (projectOutputPort.existsByNameAndOrganizationId(project.getName(), user.getOrganization().getId())) {
            throw new ProjectAlreadyExistException(ErrorMessages.PROJECT_EXIST_ALREADY, HttpStatus.CONFLICT);
        }

        project.setCreatedById(user.getId());
        log.info("Here is the user organization id before setting  " + project.getOrganizationId() + user.getOrganization().getName());

        project.setOrganizationId(user.getOrganization().getId());
        log.info("Here is the user organization id after setting  " + project.getOrganizationId() + user.getOrganization().getName());

        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        return projectOutputPort.saveProject(project);
    }

    @Override
    public Page<Project> getAllProjects(Long organizationId, Pageable pageable) {
        if (organizationId == null) {
            throw new OrganizationNotFoundException(ErrorMessages.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return projectOutputPort.getAllProjects(organizationId, pageable);
    }

    @Override
    public Project getProjectById(User user, Long projectId) {
        log.info("Retrieving project with ID: {} for user: {}", projectId, user.getId());

        validateUserExists(user);
        validateProjectId(projectId);

        Project project = projectOutputPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(ErrorMessages.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND));

        validateUserBelongsToProjectOrganization(user, project);

        log.info("Successfully retrieved project: {} for user: {}", project.getName(), user.getId());
        return project;
    }
    @Override
    public void assignUserToProject(Long projectId, Long userId, User performingUser) {
        if (!Role.ADMIN.equals(performingUser.getRole())) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }

        Project project = projectOutputPort.getProjectById(projectId);
        User userToAssign = userOutputPort.getUserById(userId);

        if (!project.getOrganizationId().equals(userToAssign.getOrganization().getId())) {
            throw new IllegalArgumentException("User and project do not belong to the same organization");
        }

        if (project.getTeamMembers() == null) {
            project.setTeamMembers(new ArrayList<>());
        }

        if (project.getTeamMembers().contains(userToAssign)) {
            throw new ProjectException(ErrorMessages.USER_ALREADY_ASSIGNED_TO_PROJECT, HttpStatus.CONFLICT);
        }

        project.getTeamMembers().add(userToAssign);
        projectOutputPort.saveProject(project);
    }


    @Override
    public Project updateProject(User user, Long projectId, Project updatedProject) {
        log.info("Updating project with ID: {} for user: {}", projectId, user.getId());

        validateUserExists(user);
        validateUserIsAdmin(user);
        validateProjectId(projectId);

        Project existingProject = projectOutputPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(ErrorMessages.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND));

        validateUserBelongsToProjectOrganization(user, existingProject);

        validateName(updatedProject.getName(), "project name");
        validateName(updatedProject.getDescription(), "project description");

        if (projectOutputPort.existsByNameAndOrganizationIdAndNotId(
                updatedProject.getName(),
                user.getOrganization().getId(),
                projectId)) {
            throw new ProjectAlreadyExistException(ErrorMessages.PROJECT_EXIST_ALREADY, HttpStatus.CONFLICT);
        }

        existingProject.setName(updatedProject.getName());
        existingProject.setDescription(updatedProject.getDescription());
        existingProject.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectOutputPort.saveProject(existingProject);
        log.info("Successfully updated project: {} for user: {}", savedProject.getName(), user.getId());

        return savedProject;
    }

    @Override
    public void deleteProject(User user, Long projectId) {
        log.info("Deleting project with ID: {} for user: {}", projectId, user.getId());

        validateUserExists(user);
        validateUserIsAdmin(user);
        validateProjectId(projectId);

        Project project = projectOutputPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(ErrorMessages.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND));

        validateUserBelongsToProjectOrganization(user, project);

        projectOutputPort.deleteProject(project);
        log.info("Successfully deleted project: {} for user: {}", project.getName(), user.getId());
    }

    @Override
    public List<User> getAllUsersInAProject(User user, Long projectId) {
        Project project = getProjectById(user,projectId);
        return project.getTeamMembers();
    }

    @Override
    public void removeUserFromProject(User user, Long projectId, Long userToBeRemoveId) {

        validateUserExists(user);
        if (!userOutputPort.existById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        validateUserIsAdmin(user);

        Project project = projectOutputPort.getProjectById(projectId);
        User userToBeRemoved = userOutputPort.getUserById(userToBeRemoveId);
        if(project.getTeamMembers().stream().map(User::getId).anyMatch(user.getId()::equals) && project.getTeamMembers().stream().map(User::getId).anyMatch(userToBeRemoved.getId()::equals )){
            projectOutputPort.removeUserFromProject(userToBeRemoved,project);
        }
    }

    private void validateUserExists(User user) {
        if (user == null || user.getId() == null) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (!userOutputPort.existById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    private void validateUserIsAdmin(User user) {
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
    }

    private void validateProjectId(Long projectId) {
        if (projectId == null) {
            throw new ProjectNotFoundException("Project ID is required", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUserBelongsToProjectOrganization(User user, Project project) {
        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new OrganizationNotFoundException(ErrorMessages.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (!user.getOrganization().getId().equals(project.getOrganizationId())) {
            log.warn("User {} from organization {} attempted to access project {} from organization {}",
                    user.getId(), user.getOrganization().getId(), project.getId(), project.getOrganizationId());
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
    }

}