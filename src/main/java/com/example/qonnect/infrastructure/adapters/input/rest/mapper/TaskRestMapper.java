package com.example.qonnect.infrastructure.adapters.input.rest.mapper;

import com.example.qonnect.domain.models.Task;

import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateTaskRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.UpdateTaskRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.TaskResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskRestMapper {

//    @Mapping(target = "project.id", source = "projectId")
    Task toTask(CreateTaskRequest request);

    Task toTask(UpdateTaskRequest updateTaskRequest);

    TaskResponse toTaskResponse(Task task);

    void updateTaskFromRequest(UpdateTaskRequest request, @MappingTarget Task task);

}
