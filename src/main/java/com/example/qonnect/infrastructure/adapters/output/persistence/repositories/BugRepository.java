package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.BugEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugRepository extends JpaRepository<BugEntity, Long> {
}
