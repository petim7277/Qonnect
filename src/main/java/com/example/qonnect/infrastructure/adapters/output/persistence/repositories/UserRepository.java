package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByInviteToken(String inviteToken);

    Page<UserEntity> findUserEntitiesByOrganizationId(Long organizationId, Pageable pageable);
}
