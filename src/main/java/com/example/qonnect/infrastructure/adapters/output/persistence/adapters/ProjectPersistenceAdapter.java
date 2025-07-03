package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.ProjectPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@AllArgsConstructor
public class ProjectPersistenceAdapter implements ProjectOutputPort {
    private final ProjectRepository projectRepository;
    private final ProjectPersistenceMapper projectPersistenceMapper;
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
}
