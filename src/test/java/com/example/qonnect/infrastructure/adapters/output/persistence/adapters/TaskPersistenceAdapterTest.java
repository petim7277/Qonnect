package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.exceptions.TaskNotFoundException;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.enums.TaskStatus;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.TaskEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskPersistenceAdapterIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskPersistenceAdapter taskPersistenceAdapter;

    private Task testTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        testTask = Task.builder()
                .title("Integration Task")
                .description("This is an integration test")
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskPersistenceAdapter.saveTask(testTask);
    }

    @Test
    void testSaveTask_Success() {
        Task newTask = Task.builder()
                .title("New Save Task")
                .description("Saving task test")
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskPersistenceAdapter.saveTask(newTask);

        assertNotNull(savedTask);
        assertNotNull(savedTask.getId());
        assertEquals("New Save Task", savedTask.getTitle());
        assertEquals("Saving task test", savedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, savedTask.getStatus());

        TaskEntity entity = taskRepository.findByTitle("New Save Task").orElse(null);
        assertNotNull(entity);
        assertEquals("Saving task test", entity.getDescription());
    }

    @Test
    void testGetTaskByName_Success() {
        Task found = taskPersistenceAdapter.getTaskByTitle("Integration Task");

        assertNotNull(found);
        assertEquals("Integration Task", found.getTitle());
        assertEquals("This is an integration test", found.getDescription());
    }

    @Test
    void testGetTaskByName_NotFound() {
        assertThrows(TaskNotFoundException.class, () ->
                taskPersistenceAdapter.getTaskByTitle("Non-existent Task"));
    }
}
