package com.example.qonnect.infrastructure.adapters.input.rest.mapper;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.RegisterUserRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.RegisterUserResponse;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserRestMapper {


    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(RegisterUserRequest registerUserRequest);


    RegisterUserResponse toCreateUserResponse(User user);


    UserProfileResponse toUserProfileResponse(User profile);

}
