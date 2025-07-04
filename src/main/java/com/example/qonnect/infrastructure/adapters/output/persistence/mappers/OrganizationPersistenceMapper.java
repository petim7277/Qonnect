package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrganizationPersistenceMapper {

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "projects", ignore = true)
    Organization toOrganization(OrganizationEntity organizationEntity);

    @Named("toOrganizationWithoutProjects")
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "projects", ignore = true)
    Organization toOrganizationWithoutProjects(OrganizationEntity organizationEntity);

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "projects", ignore = true)
    OrganizationEntity toOrganizationEntity(Organization organization);
}


