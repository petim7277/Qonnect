package com.example.qonnect.infrastructure.adapters.input.rest.mapper;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateProjectRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.ProjectCreationResponse;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectRestMapper {

    Project toDomain(@Valid CreateProjectRequest request);

    ProjectCreationResponse toResponse(Project created);

}
