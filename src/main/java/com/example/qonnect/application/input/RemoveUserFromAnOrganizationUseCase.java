package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;

public interface RemoveUserFromAnOrganizationUseCase {

    void removeUserFromAnOrganization(User user, Long userToBeRemovedId,Long organizationId);
}
