package com.example.qonnect.infrastructure.adapters.input.rest.mapper;


import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.CreateBugRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.UpdateBugRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.AssignBugResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.BugResponse;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BugRestMapper {


    @Mapping(target = "id", source = "id")
    Bug toDomain(UpdateBugRequest request);

    @Mapping(target = "status", ignore = true)
    Bug toDomain(@Valid CreateBugRequest request);

    BugResponse toResponse(Bug bug);

    BugResponse toBugResponse(Bug assignedBug);

    AssignBugResponse toAssignBugResponse(Bug assignedBug);
}
