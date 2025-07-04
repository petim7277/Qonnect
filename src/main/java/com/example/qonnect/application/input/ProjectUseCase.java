package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectUseCase {

    Project createProject(User user, Project project);

    Page<Project> getAllProjects(Long organizationId, Pageable pageable);

    Project getProjectById(User user, Long projectId);

    Project updateProject(User user, Long projectId, Project updatedProject);

    void deleteProject(User user, Long projectId);
}