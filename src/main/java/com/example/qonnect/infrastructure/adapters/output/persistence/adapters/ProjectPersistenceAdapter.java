package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.ProjectPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OrganizationRepository;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectPersistenceAdapter implements ProjectOutputPort {

    private final ProjectPersistenceMapper projectPersistenceMapper;
    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public Project saveProject(Project project) {
        if (project.getId() != null && !projectRepository.existsById(project.getId())) {
            throw new ProjectNotFoundException(ErrorMessages.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

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
    public void deleteProject(Project project) {
        if (project == null || project.getId() == null) {
            throw new ProjectNotFoundException(ErrorMessages.PROJECT_ID_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Long projectId = project.getId();
        log.info("Deleting project with ID: {} and name: {}", projectId, project.getName());

        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(ErrorMessages.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        projectRepository.deleteById(projectId);
        log.debug("Project with ID: {} successfully deleted", projectId);
    }

    @Override
    public boolean existsByNameAndOrganizationId(String name, Long organizationId) {
        return projectRepository.existsProjectNameInOrganization(name, organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> getAllProjects(Long organizationId, Pageable pageable) {
        log.info("Retrieving projects for organization ID: {} with pagination: {}", organizationId, pageable);

        Page<ProjectEntity> projectEntities = projectRepository.findByOrganizationId(organizationId, pageable);
        log.info("Found {} projects for organization ID: {}", projectEntities.getTotalElements(), organizationId);

        Page<Project> projects = projectEntities.map(projectPersistenceMapper::toProject);
        log.info("Mapped {} project entities to domain objects", projects.getNumberOfElements());

        return projects;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        log.info("Finding project by ID: {}", id);

        Optional<ProjectEntity> projectEntity = projectRepository.findById(id);

        if (projectEntity.isPresent()) {
            Project project = projectPersistenceMapper.toProject(projectEntity.get());
            log.info("Found project: {} with ID: {}", project.getName(), id);
            return Optional.of(project);
        } else {
            log.info("No project found with ID: {}", id);
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByNameAndOrganizationIdAndNotId(String name, Long organizationId, Long projectId) {
        log.info("Checking if project name '{}' exists in organization {} excluding project ID: {}",
                name, organizationId, projectId);

        return projectRepository.existsProjectNameInOrganizationExcludingId(name, organizationId, projectId);
    }

    @Override
    public void removeUserFromProject(User userToBeRemoved, Project project) {
            if (project.getTeamMembers() == null) {
                project.setTeamMembers(new ArrayList<>());
            }
            project.getTeamMembers().removeIf(u -> u.getId().equals(userToBeRemoved.getId()));
            projectRepository.save(projectPersistenceMapper.toProjectEntity(project));
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId).orElseThrow(()->new ProjectNotFoundException(ErrorMessages.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        return projectPersistenceMapper.toProject(project);
    }







}