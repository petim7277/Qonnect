package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.exceptions.TaskNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.enums.TaskStatus;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.TaskEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.ProjectPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.TaskRepository;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskPersistenceAdapterTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskPersistenceAdapter taskPersistenceAdapter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectPersistenceAdapter projectPersistenceAdapter;
    @Autowired
    private UserPersistenceAdapter userPersistenceAdapter;
    private boolean skipCleanup = false;
    private Task savedTask;
    @Autowired
    private OrganizationPersistenceAdapter organizationPersistenceAdapter;


    @BeforeEach
    void setUp() {


        Task entity = new Task();
        entity.setTitle("Integration Task");
        entity.setDescription("This is an integration test");
        entity.setStatus(TaskStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        savedTask = taskPersistenceAdapter.saveTask(entity);
    }

    @AfterEach
    void tearDown() {
        if (skipCleanup) {
            return;
        }
        taskPersistenceAdapter.deleteTaskById(savedTask.getId());
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
        taskPersistenceAdapter.deleteTaskById(saved.getId());
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

        skipCleanup = true;

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
      Organization  organization = Organization.builder()
                .name("Test Organization")
                .build();

      Organization savedOrg = organizationPersistenceAdapter.saveOrganization(organization);

        Project project = Project.builder()
                .name("project")
                .organizationId(savedOrg.getId())
                .build();
      Project savedProject=  projectPersistenceAdapter.saveProject(project);


        Task task1 = Task.builder()
                .title("Task 1")
                .description("Desc 1")
                .projectId(project.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task task2 = Task.builder()
                .title("Task 2")
                .description("Desc 2")
                .projectId(project.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        Task saved1 = taskPersistenceAdapter.saveTask(task1);
        Task saved2 = taskPersistenceAdapter.saveTask(task2);

        List<Task> taskList = taskPersistenceAdapter.getAllTasksByProjectId(project.getId());

        assertEquals(3, taskList.size());
        assertTrue(taskList.stream().anyMatch(t -> t.getTitle().equals("Task 1")));
        assertTrue(taskList.stream().anyMatch(t -> t.getTitle().equals("Task 2")));

        projectPersistenceAdapter.deleteProjectById(savedProject.getId());
        taskPersistenceAdapter.deleteTaskById(saved1.getId());
        taskPersistenceAdapter.deleteTaskById(saved2.getId());
        organizationPersistenceAdapter.deleteById(savedOrg.getId());
    }

    @Test
    void testGetTasksByUserId_WithPagination_Success() {


        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testEmail@gmail.com");
        user.setPassword("pass");
        user.setEnabled(true);
        user.setInvited(false);
        user.setRole(Role.DEVELOPER);


        User savedUser = userPersistenceAdapter.saveUser(user);

        Task task1 = Task.builder()
                .title("Assigned Task 1")
                .description("Description 1")
                .assignedTo(savedUser)
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task task2 = Task.builder()
                .title("Assigned Task 2")
                .description("Description 2")
                .assignedTo(savedUser)
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask1 = taskPersistenceAdapter.saveTask(task1);
        Task savedTask2= taskPersistenceAdapter.saveTask(task2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Task> result = taskPersistenceAdapter.getTasksByUserId(savedUser.getId(), pageable);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(t -> t.getTitle().equals("Assigned Task 1")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getTitle().equals("Assigned Task 2")));

        taskPersistenceAdapter.deleteTaskById(savedTask1.getId());
        taskPersistenceAdapter.deleteTaskById(savedTask2.getId());
        userPersistenceAdapter.deleteUserById(savedUser.getId());

    }


}
