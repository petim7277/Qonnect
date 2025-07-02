package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectPersistenceMapper {
    Project  toProject(ProjectEntity project);

    @Mapping(source = "organization" ,target = "organization")
    ProjectEntity  toProjectEntity(Project project);
}
