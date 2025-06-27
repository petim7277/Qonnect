package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserPersistenceMapper {

    @Mapping(target = "organization.users",  ignore = true)
    @Mapping(target = "organization.projects", ignore = true)
    @Mapping(target = "password", source = "password")
    User toUser(UserEntity entity);

    UserEntity toUserEntity(User user);
}
