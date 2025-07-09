package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.TaskEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                UserPersistenceMapper.class,
                ProjectPersistenceMapper.class,
                BugPersistenceMapper.class
        },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskPersistenceMapper {

    @Mapping(source = "projectId", target = "projectId")
    Task toTask(TaskEntity entity);

    @Mapping(target = "projectId", source = "projectId")
    TaskEntity toTaskEntity(Task task);

    List<Task> toTaskList(List<TaskEntity> entities);

    List<TaskEntity> toTaskEntityList(List<Task> tasks);
}



