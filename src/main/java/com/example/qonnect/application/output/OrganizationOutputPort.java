package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Organization;

public interface OrganizationOutputPort {

    Organization saveOrganization(Organization organization);


    Organization getOrganizationByName(String name);

    boolean existsByName(String name);

}
