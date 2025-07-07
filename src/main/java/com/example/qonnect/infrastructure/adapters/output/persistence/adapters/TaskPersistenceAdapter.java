package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.domain.exceptions.TaskNotFoundException;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.TaskEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.TaskPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class TaskPersistenceAdapter implements TaskOutputPort {

    private final TaskRepository taskRepository;
    private final TaskPersistenceMapper taskMapper;

    @Override
    public Task saveTask(Task task) {
        TaskEntity entity = taskMapper.toTaskEntity(task);
        TaskEntity savedEntity = taskRepository.save(entity);
        return taskMapper.toTask(savedEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public Task getTaskByTitle(String title) {
        TaskEntity task = taskRepository.findByTitle(title)
                .orElseThrow(() -> new TaskNotFoundException(ErrorMessages.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
        return taskMapper.toTask(task);
    }

    @Override
    public boolean existsByTitleAndProjectId(String title, Long projectId) {
        return taskRepository.existsByTitleAndProjectId(title,projectId);
    }

    @Override
    public void deleteTaskById(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(ErrorMessages.TASK_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        taskRepository.deleteById(taskId);
    }

    @Override
    public Task getTaskById(Long taskId) {
        TaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(ErrorMessages.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
        return taskMapper.toTask(entity);
    }



}
