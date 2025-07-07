package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserOutputPort {
    User getUserByEmail(String email);


    User saveUser(User user);

    boolean userExistsByEmail(String email);

    boolean existById(Long id);

    User getUserByInviteToken(String inviteToken);

    User getUserById(Long userId);

    Page<User> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
