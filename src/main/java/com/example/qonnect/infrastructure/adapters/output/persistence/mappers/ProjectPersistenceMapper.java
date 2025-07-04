package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectPersistenceMapper {

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(target = "teamMembers", ignore = true)
    @Mapping(target = "bugs", ignore = true)
    Project toProject(ProjectEntity project);

    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "teamMembers", ignore = true)
    @Mapping(target = "bugs", ignore = true)
    ProjectEntity toProjectEntity(Project project);
}

