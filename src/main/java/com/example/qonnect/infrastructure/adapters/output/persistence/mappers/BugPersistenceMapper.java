package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.BugEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { ProjectPersistenceMapper.class, UserPersistenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BugPersistenceMapper {


    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "status", target = "status")
    Bug toBug(BugEntity entity);

    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "task.id", source = "taskId")
    BugEntity toBugEntity(Bug domain);


    List<Bug> toBugList(List<BugEntity> entities);

    List<BugEntity> toBugEntityList(List<Bug> bugs);
}
