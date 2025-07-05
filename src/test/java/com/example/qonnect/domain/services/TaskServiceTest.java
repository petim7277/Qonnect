package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.TaskAlreadyExistException;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.enums.TaskStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskService taskService;
    private UserOutputPort userOutputPort;
    private TaskOutputPort taskOutputPort;
    private ProjectOutputPort projectOutputPort;

    private User adminUser;
    private User regularUser;
    private Project testProject;

    @BeforeEach
    void setup() {
        userOutputPort = mock(UserOutputPort.class);
        taskOutputPort = mock(TaskOutputPort.class);
        projectOutputPort = mock(ProjectOutputPort.class);

        taskService = new TaskService( taskOutputPort,userOutputPort, projectOutputPort);

        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        regularUser = User.builder()
                .id(2L)
                .email("user@example.com")
                .role(Role.DEVELOPER)
                .build();

        testProject = Project.builder()
                .id(10L)
                .name("Project A")
                .build();
    }

    @Test
    void testCreateTask_AsAdmin_Success() {
        Task task = Task.builder()
                .title("New Task")
                .description("Task Description")
                .status(TaskStatus.PENDING)
                .project(testProject)
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.existsByTitleAndProjectId(task.getTitle(), testProject.getId())).thenReturn(false);
        when(taskOutputPort.saveTask(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Task created = taskService.createTask(adminUser, task);

        assertNotNull(created.getId());
        assertEquals("New Task", created.getTitle());
        verify(taskOutputPort).saveTask(any(Task.class));
    }

    @Test
    void testCreateTask_AsNonAdmin_ThrowsAccessDenied() {
        Task task = Task.builder()
                .title("Task")
                .description("desc")
                .project(testProject)
                .build();

        when(userOutputPort.getUserByEmail(regularUser.getEmail())).thenReturn(regularUser);

        assertThrows(AccessDeniedException.class, () -> {
            taskService.createTask(regularUser, task);
        });
    }

    @Test
    void testCreateTask_WithExistingTitleInProject_Throws() {
        Task task = Task.builder()
                .title("Duplicate")
                .description("desc")
                .project(testProject)
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.existsByTitleAndProjectId(task.getTitle(), testProject.getId())).thenReturn(true);

        assertThrows(TaskAlreadyExistException.class, () -> {
            taskService.createTask(adminUser, task);
        });
    }
}
