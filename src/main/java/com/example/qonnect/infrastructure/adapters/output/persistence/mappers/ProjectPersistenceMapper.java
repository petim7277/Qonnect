package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = { UserPersistenceMapper.class }
)
public interface ProjectPersistenceMapper {

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "teamMembers", target = "teamMembers", qualifiedByName = "toUserListWithoutProjects")
    @Mapping(target = "bugs", ignore = true)
    Project toProject(ProjectEntity project);

    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "teamMembers", ignore = true)
    @Mapping(target = "bugs", ignore = true)
    ProjectEntity toProjectEntity(Project project);
}


