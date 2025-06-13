package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
}
