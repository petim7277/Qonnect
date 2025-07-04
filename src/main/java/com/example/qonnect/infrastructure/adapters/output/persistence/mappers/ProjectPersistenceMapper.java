package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {UserPersistenceMapper.class}
)
public interface ProjectPersistenceMapper {

    @Mapping(source = "organization.id", target = "organizationId")
    Project toProject(ProjectEntity project);

    @Mapping(target = "organization", ignore = true) 
    ProjectEntity toProjectEntity(Project project);

    @AfterMapping
    default void initTeamMembers(@MappingTarget Project project) {
        if (project.getTeamMembers() == null) {
            project.setTeamMembers(new java.util.ArrayList<>());
        }
    }
}
