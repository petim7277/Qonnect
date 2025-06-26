package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.ProjectPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectPersistenceAdapter implements ProjectOutputPort {

    private final ProjectPersistenceMapper projectPersistenceMapper;
    private final ProjectRepository projectRepository;


    @Override
    public Project saveProject(Project project) {
        log.info("Saving project: {}", project);

        ProjectEntity entity = projectPersistenceMapper.toProjectEntity(project);
        log.info("Mapped to entity: {}", entity);

        entity = projectRepository.save(entity);
        log.info("Saved entity: {}", entity);

        Project savedProject = projectPersistenceMapper.toProject(entity);
        log.info("Mapped back to domain project: {}", savedProject);

        return savedProject;
    }

    @Override
    public boolean existById(Long id) {
        return projectRepository.existsById(id);
    }
}
