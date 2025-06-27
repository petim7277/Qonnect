package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByName(String name);


    boolean existsByName(String name);
}
