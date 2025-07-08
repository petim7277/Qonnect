package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ViewUserUserCase {
    Page<User> getAllUsersInOrganization(User user,Long organizationId, Pageable pageable);

}
