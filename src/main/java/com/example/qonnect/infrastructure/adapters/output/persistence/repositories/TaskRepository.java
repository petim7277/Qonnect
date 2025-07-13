package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.BugEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.TaskEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findByTitle(String title);

    boolean existsByTitleAndProjectId(String title, Long projectId);

    List<TaskEntity> findAllByProjectId(Long projectId);

    Page<TaskEntity> findByAssignedTo(UserEntity assignedTo, Pageable pageable);

    Page<TaskEntity> findByAssignedTo_Id(Long assignedToId, Pageable pageable);
}
