package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProjectOutputPort {

    Project saveProject(Project project);

    boolean existById(Long id);

    boolean existsByNameAndOrganizationId(String name, Long organizationId);

    Page<Project> getAllProjects(Long organizationId, Pageable pageable);

    void deleteProject(Project project);

    Optional<Project> findById(Long id);

    Project getProjectById(Long projectId);

    boolean existsByNameAndOrganizationIdAndNotId(String name, Long organizationId, Long projectId);

    void removeUserFromProject(User userToBeRemoved, Project project);
}