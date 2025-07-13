package com.example.qonnect.infrastructure.adapters.input.rest.mapper;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.data.requests.RegisterOrganizationAdminRequest;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.RegisterOrganizationAdminResponse;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationRestMapper {


    @Mapping(source = "organizationName", target = "name")
    Organization toOrganization(RegisterOrganizationAdminRequest request);

    User toUser( RegisterOrganizationAdminRequest request);

    @Mapping(source = "registeredAdmin.email", target = "email")
    @Mapping(source = "org.name",            target = "organizationName")
    @Mapping(target = "message",   constant = "Organization and admin user registered successfully.")
    RegisterOrganizationAdminResponse toRegisterResponse(User registeredAdmin, Organization org);

}
