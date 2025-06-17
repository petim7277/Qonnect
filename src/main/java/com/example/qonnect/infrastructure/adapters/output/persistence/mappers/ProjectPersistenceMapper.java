package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectPersistenceMapper {
    Project  toProject(ProjectEntity project);
}
