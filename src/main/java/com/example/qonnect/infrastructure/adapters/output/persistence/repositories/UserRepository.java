package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
