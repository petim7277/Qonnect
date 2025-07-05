package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;

public interface OrganizationOutputPort {

    Organization saveOrganization(Organization organization);


    Organization getOrganizationByName(String name);

    boolean existsByName(String name);

    Organization getOrganizationById(Long id);

    void removeUserFromOrganization(User userToBeRemoved);
}
