package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.OrganizationPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.ProjectPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OrganizationRepository;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectPersistenceAdapter implements ProjectOutputPort {

    private final ProjectPersistenceMapper projectPersistenceMapper;
    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;


    @Override
    public Project saveProject(Project project) {
        OrganizationEntity orgEntity = organizationRepository.findById(project.getOrganizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(ErrorMessages.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND));

        ProjectEntity entity = projectPersistenceMapper.toProjectEntity(project);

        entity.setOrganization(orgEntity);

        entity = projectRepository.save(entity);
        return projectPersistenceMapper.toProject(entity);
    }


    @Override
    public boolean existById(Long id) {
        return projectRepository.existsById(id);
    }

    @Override
    public boolean existsByNameAndOrganizationId(String name, Long organizationId) {
        return projectRepository.existsProjectNameInOrganization(name, organizationId);
    }

    @Override
    public Page<Project> getAllProjects(Long organizationId, Pageable pageable) {
        log.info("Retrieving projects for organization ID: {} with pagination: {}", organizationId, pageable);

        Page<ProjectEntity> projectEntities = projectRepository.findByOrganizationId(organizationId, pageable);
        log.info("Found {} projects for organization ID: {}", projectEntities.getTotalElements(), organizationId);

        Page<Project> projects = projectEntities.map(projectPersistenceMapper::toProject);
        log.info("Mapped {} project entities to domain objects", projects.getNumberOfElements());

        return projects;
    }

}