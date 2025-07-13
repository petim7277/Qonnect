package com.example.qonnect.infrastructure.adapters.output.mapper;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.UserResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", source = "id")
    User toDomain(UserRepresentation userRepresentation);

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "role", target = "role")
    UserResponse toUserResponse(User user);




}
