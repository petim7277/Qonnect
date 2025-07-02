package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.ProjectUseCase;
import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.ProjectAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Role;
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

import static com.example.qonnect.domain.validators.InputValidator.validateName;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService implements ProjectUseCase {

    private final ProjectOutputPort projectOutputPort;
    private final UserOutputPort userOutputPort;


    @Override
    public Project createProject(User user, Project project) {

        if (!userOutputPort.existById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (!Role.ADMIN.equals(user.getRole())) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
        log.info("Here is the user organization id " + user.getOrganization());

        validateName(project.getName(), "project name");
        validateName(project.getDescription(), "project description");

        if (projectOutputPort.existsByNameAndOrganizationId(project.getName(), user.getOrganization().getId())) {
            throw new ProjectAlreadyExistException(ErrorMessages.PROJECT_EXIST_ALREADY, HttpStatus.CONFLICT);
        }

        project.setCreatedBy(user);
        log.info("Here is the user organization id before setting  " + project.getOrganization() + user.getOrganization().getName());

        project.setOrganization(user.getOrganization());
        log.info("Here is the user organization id after setting  " + project.getOrganization() + user.getOrganization().getName());

        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        return projectOutputPort.saveProject(project);
    }

    @Override
    public Page<Project> getAllProjects(Long organizationId,Pageable pageable) {
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        return projectOutputPort.getAllProjects(organizationId,pageable);
    }

}
