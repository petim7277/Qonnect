package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findByTitle(String title);

    boolean existsByTitleAndProjectId(String title, Long projectId);

}
