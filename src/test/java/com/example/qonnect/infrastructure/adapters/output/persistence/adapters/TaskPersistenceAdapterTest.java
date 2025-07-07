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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskPersistenceAdapterTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskPersistenceAdapter taskPersistenceAdapter;

    private Task savedTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        TaskEntity entity = new TaskEntity();
        entity.setTitle("Integration Task");
        entity.setDescription("This is an integration test");
        entity.setStatus(TaskStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        TaskEntity savedEntity = taskRepository.save(entity);
        savedTask = taskPersistenceAdapter.getTaskByTitle(savedEntity.getTitle());
    }

    @Test
    void testSaveTask_Success() {
        Task task = Task.builder()
                .title("Save Test")
                .description("Testing save")
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task saved = taskPersistenceAdapter.saveTask(task);

        assertNotNull(saved.getId());
        assertEquals("Save Test", saved.getTitle());
        assertEquals("Testing save", saved.getDescription());
    }

    @Test
    void testGetTaskByTitle_Success() {
        Task found = taskPersistenceAdapter.getTaskByTitle("Integration Task");

        assertNotNull(found);
        assertEquals("Integration Task", found.getTitle());
        assertEquals("This is an integration test", found.getDescription());
    }

    @Test
    void testGetTaskByTitle_NotFound() {
        assertThrows(TaskNotFoundException.class, () ->
                taskPersistenceAdapter.getTaskByTitle("Non-existent Task"));
    }

    @Test
    void testDeleteTaskById_Success() {
        Long taskId = savedTask.getId();

        assertTrue(taskRepository.existsById(taskId));

        taskPersistenceAdapter.deleteTaskById(taskId);

        assertFalse(taskRepository.existsById(taskId));
    }

    @Test
    void testDeleteTaskById_NotFound() {
        Long nonExistentId = 99999L;

        assertThrows(TaskNotFoundException.class, () ->
                taskPersistenceAdapter.deleteTaskById(nonExistentId));
    }

    @Test
    void testGetAllTasksByProjectId_Success() {
        Long projectId = 43L;

        TaskEntity task1 = TaskEntity.builder()
                .title("Task 1")
                .description("Desc 1")
                .projectId(projectId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TaskEntity task2 = TaskEntity.builder()
                .title("Task 2")
                .description("Desc 2")
                .projectId(projectId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        taskRepository.save(task1);
        taskRepository.save(task2);

        List<Task> taskList = taskPersistenceAdapter.getAllTasksByProjectId(projectId);

        assertEquals(2, taskList.size());
        assertTrue(taskList.stream().anyMatch(t -> t.getTitle().equals("Task 1")));
        assertTrue(taskList.stream().anyMatch(t -> t.getTitle().equals("Task 2")));
    }

}
