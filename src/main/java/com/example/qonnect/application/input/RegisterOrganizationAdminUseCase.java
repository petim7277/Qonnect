package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;

public interface RegisterOrganizationAdminUseCase {

    User registerOrganizationAdmin(User user, Organization organization);
}
