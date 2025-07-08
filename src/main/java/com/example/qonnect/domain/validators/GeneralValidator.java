package com.example.qonnect.domain.validators;

import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@RequiredArgsConstructor
public class GeneralValidator {

    public static void validateUserExists(User user) {
        if (user == null || user.getId() == null) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    public static void validateUserIsAdmin(User user) {
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
    }

    public static void validateProjectId(Long projectId) {
        if (projectId == null) {
            throw new ProjectNotFoundException("Project ID is required", HttpStatus.BAD_REQUEST);
        }
    }

    public static void validateUserBelongsToProjectOrganization(User user, Project project) {
        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new OrganizationNotFoundException(ErrorMessages.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (!user.getOrganization().getId().equals(project.getOrganizationId())) {
            log.warn("User {} from organization {} attempted to access project {} from organization {}",
                    user.getId(), user.getOrganization().getId(), project.getId(), project.getOrganizationId());
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
    }


    public static void validateUserBelongsOrganization(User user, Long organizationId) {
        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new OrganizationNotFoundException(ErrorMessages.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (!user.getOrganization().getId().equals(organizationId)) {
            log.warn("User {} from organization {} attempted to access organization {} from organization {}",
                    user.getId(), user.getOrganization().getId(), organizationId, organizationId);
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_TO_ORGANIZATION);
        }
    }
}
