package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { UserPersistenceMapper.class })
public interface ProjectPersistenceMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "teamMembers", target = "teamMembers", qualifiedByName = "toUserListWithoutProjects")
    @Mapping(target = "bugs", ignore = true)
    Project toProject(ProjectEntity project);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "teamMembers", ignore = true)
    @Mapping(target = "bugs", ignore = true)
    ProjectEntity toProjectEntity(Project project);
}



