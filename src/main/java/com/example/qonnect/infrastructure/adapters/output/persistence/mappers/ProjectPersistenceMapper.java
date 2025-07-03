package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectPersistenceMapper {

    // Break circular references when mapping from entity to domain
    @Mapping(target = "organization.users", ignore = true)          // Don't map users in organization
    @Mapping(target = "organization.projects", ignore = true)       // Don't map projects in organization
    @Mapping(target = "teamMembers.organization", ignore = true)    // Don't map organization in team members
    @Mapping(target = "createdBy.organization", ignore = true)      // Don't map organization in createdBy user
    Project toProject(ProjectEntity project);

    // When saving, we don't need to deeply map nested collections
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "organization", target = "organization")
    @Mapping(target = "organization.users", ignore = true)          // Don't try to save users when saving project
    @Mapping(target = "organization.projects", ignore = true)       // Don't try to save projects when saving project
    ProjectEntity toProjectEntity(Project project);
}