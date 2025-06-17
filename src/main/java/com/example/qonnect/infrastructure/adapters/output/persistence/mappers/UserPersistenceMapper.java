package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserPersistenceMapper {
    User toUser(UserEntity userEntity) ;

}
