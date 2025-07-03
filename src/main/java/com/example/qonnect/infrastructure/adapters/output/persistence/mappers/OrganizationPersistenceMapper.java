package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationPersistenceMapper {


    Organization toOrganization(OrganizationEntity organizationEntity);

    OrganizationEntity toOrganizationEntity(Organization organization);

}
