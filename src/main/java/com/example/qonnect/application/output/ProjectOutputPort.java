package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.qonnect.domain.models.Project;

public interface ProjectOutputPort {

    Project saveProject(Project project);

    boolean existById(Long id);

    boolean existsByNameAndOrganizationId(String name, Long organizationId);


    Page<Project> getAllProjects(Long organizationId, Pageable pageable);
    void deleteProject(Project project);
}
