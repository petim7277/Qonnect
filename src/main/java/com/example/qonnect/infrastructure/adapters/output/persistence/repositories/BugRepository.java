package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.infrastructure.adapters.output.persistence.entities.BugEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BugRepository extends JpaRepository<BugEntity, Long> {

    Optional<BugEntity> findByIdAndTaskId(Long id, Long taskId);


    Page<BugEntity> findAllByProject_Id(Long projectId, Pageable pageable);
    Page<BugEntity> findAllByTask_Id(Long taskId, Pageable pageable);


    boolean existsByTitleAndProjectId(String title, Long projectId);

    Page<BugEntity> findByAssignedTo_Id(Long userId, Pageable pageable);
}
