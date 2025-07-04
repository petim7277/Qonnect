package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { OrganizationPersistenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserPersistenceMapper {

    // FULL user, but with a slim Organization and a slim Project
    @Mapping(target = "organization", qualifiedByName = "toOrganizationWithoutProjects")
    @Mapping(target = "projects.teamMembers", ignore = true)   // keep project slim
    User toUser(UserEntity entity);

    // Slim user (no projects at all) – used inside Project mapping
    @Named("toUserWithoutProjects")
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "organization", qualifiedByName = "toOrganizationWithoutProjects")
    User toUserWithoutProjects(UserEntity entity);

    @Mapping(target = "organization.users", ignore = true)
    @Mapping(target = "projects.teamMembers", ignore = true)
    UserEntity toUserEntity(User user);

    List<User> toUserList(List<UserEntity> entities);

    // Tell MapStruct to use the slim mapper for every element
    @Named("toUserListWithoutProjects")
    @IterableMapping(qualifiedByName = "toUserWithoutProjects")
    List<User> toUserListWithoutProjects(List<UserEntity> entities);
}



