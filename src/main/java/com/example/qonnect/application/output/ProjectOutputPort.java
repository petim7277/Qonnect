package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Project;

public interface ProjectOutputPort {

    Project saveProject(Project project);

    boolean existById(Long id);

    boolean existsByNameAndOrganizationId(String name, Long organizationId);


}
