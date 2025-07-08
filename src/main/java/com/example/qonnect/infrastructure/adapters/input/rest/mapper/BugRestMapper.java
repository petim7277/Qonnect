package com.example.qonnect.infrastructure.adapters.input.rest.mapper;


import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.UpdateBugRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.BugResponse;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BugRestMapper {


    Bug toDomain(@Valid UpdateBugRequest request);

    BugResponse toResponse(Bug bug);
}
