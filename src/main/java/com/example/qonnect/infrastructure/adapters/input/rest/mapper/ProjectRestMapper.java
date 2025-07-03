package com.example.qonnect.infrastructure.adapters.input.rest.mapper;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateProjectRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.ProjectCreationResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.ProjectResponse;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ProjectRestMapper {


    Project toDomain(@Valid CreateProjectRequest request);

    ProjectCreationResponse toResponse(Project created);

    ProjectResponse toProjectResponse(Project project);
}
