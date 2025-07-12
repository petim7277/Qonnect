package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.BugOutputPort;
import com.example.qonnect.domain.exceptions.BugNotFoundException;
import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.BugEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.BugPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.BugRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BugPersistenceAdapter implements BugOutputPort {

    private final BugPersistenceMapper bugPersistenceMapper;
    private final BugRepository bugRepository;

    @Override
    @Transactional(readOnly = true)
    public Bug getBugByIdAndTaskId(Long id, Long taskId) {
        log.info("Getting bug with ID: {} for task ID: {}", id, taskId);

        Optional<BugEntity> bugEntity = bugRepository.findByIdAndTaskId(id, taskId);

        if (bugEntity.isEmpty()) {
            log.warn("Bug not found with ID: {} for task ID: {}", id, taskId);
            throw new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Bug bug = bugPersistenceMapper.toBug(bugEntity.get());
        log.info("Successfully retrieved bug: {} for task: {}", bug.getId(), taskId);
        return bug;
    }

    @Override
    @Transactional
    public Bug saveBug(Bug bug) {
        log.info("Saving bug: {}", bug.getId());

        BugEntity entity = bugPersistenceMapper.toBugEntity(bug);

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        entity = bugRepository.save(entity);

        Bug savedBug = bugPersistenceMapper.toBug(entity);
        log.info("Successfully saved bug with ID: {}", savedBug.getId());
        return savedBug;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getAllBugsByProjectId(Long projectId, Pageable pageable) {
        log.info("Getting all bugs for project ID: {} with pagination: {}", projectId, pageable);

        Page<BugEntity> bugEntities = bugRepository.findAllByProject_Id(projectId, pageable);

        Page<Bug> bugs = bugEntities.map(bugPersistenceMapper::toBug);
        log.info("Found {} bugs for project ID: {}", bugs.getTotalElements(), projectId);

        return bugs;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getAllBugsByTaskId(Long taskId, Pageable pageable) {
        log.info("Getting all bugs for task ID: {} with pagination: {}", taskId, pageable);

        Page<BugEntity> bugEntities = bugRepository.findAllByTask_Id(taskId, pageable);

        Page<Bug> bugs = bugEntities.map(bugPersistenceMapper::toBug);
        log.info("Found {} bugs for task ID: {}", bugs.getTotalElements(), taskId);

        return bugs;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getBugsByUserId(Long userId, Pageable pageable) {
        log.info("Getting bugs for user ID: {} with pagination: {}", userId, pageable);

        Page<BugEntity> bugEntities = bugRepository.findByAssignedTo_Id(userId, pageable);

        Page<Bug> bugs = bugEntities.map(bugPersistenceMapper::toBug);
        log.info("Found {} bugs for user ID: {}", bugs.getTotalElements(), userId);

        return bugs;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return bugRepository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteBug(Long id) {
        log.info("Deleting bug with ID: {}", id);

        if (!bugRepository.existsById(id)) {
            throw new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        bugRepository.deleteById(id);
        log.info("Successfully deleted bug with ID: {}", id);
    }

    @Override
    public boolean existsByTitleAndProjectId(String title, Long projectId) {
        return bugRepository.existsByTitleAndProjectId(title,projectId);
    }

    @Override
    public Bug getBugById(Long bugId) {
        BugEntity entity = bugRepository.findById(bugId).orElseThrow(()->new BugNotFoundException(ErrorMessages.BUG_NOT_FOUND, HttpStatus.NOT_FOUND));
        return bugPersistenceMapper.toBug(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getBugsByCreatedById(Long userId, Pageable pageable) {
        Page<BugEntity> bugEntities = bugRepository.findByCreatedBy_Id(userId, pageable);
        return bugEntities.map(bugPersistenceMapper::toBug);
    }

}