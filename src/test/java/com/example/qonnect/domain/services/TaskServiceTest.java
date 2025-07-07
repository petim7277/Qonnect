package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.TaskAlreadyExistException;
import com.example.qonnect.domain.exceptions.TaskNotFoundException;
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
        userOutputPort    = mock(UserOutputPort.class);
        taskOutputPort    = mock(TaskOutputPort.class);
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
                .projectId(testProject.getId())
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.existsByTitleAndProjectId(task.getTitle(), testProject.getId())).thenReturn(false);
        when(taskOutputPort.saveTask(any(Task.class))).thenAnswer(inv -> {
            Task saved = inv.getArgument(0);
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
                .projectId(testProject.getId())
                .build();

        when(userOutputPort.getUserByEmail(regularUser.getEmail())).thenReturn(regularUser);

        assertThrows(AccessDeniedException.class, () ->
                taskService.createTask(regularUser, task)
        );
    }

    @Test
    void testCreateTask_WithExistingTitleInProject_Throws() {
        Task task = Task.builder()
                .title("Duplicate")
                .description("desc")
                .projectId(testProject.getId())
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.existsByTitleAndProjectId(task.getTitle(), testProject.getId())).thenReturn(true);

        assertThrows(TaskAlreadyExistException.class, () ->
                taskService.createTask(adminUser, task)
        );
    }


    @Test
    void deleteTask_AsAdmin_Success() {
        long taskId = 100L;
        Task task = Task.builder()
                .id(taskId)
                .title("To‑Delete")
                .projectId(testProject.getId())
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(task);

        assertDoesNotThrow(() ->
                taskService.deleteTask(adminUser, testProject.getId(), taskId)
        );

        verify(taskOutputPort).deleteTaskById(taskId);
    }

    @Test
    void deleteTask_AsNonAdmin_ThrowsAccessDenied() {
        long taskId = 101L;

        when(userOutputPort.getUserByEmail(regularUser.getEmail())).thenReturn(regularUser);

        assertThrows(AccessDeniedException.class, () ->
                taskService.deleteTask(regularUser, testProject.getId(), taskId)
        );

        verify(taskOutputPort, never()).deleteTaskById(anyLong());
    }

    @Test
    void deleteTask_TaskNotInProject_ThrowsNotFound() {
        long taskId = 102L;
        Project otherProject = Project.builder().id(99L).name("Other").build();

        Task task = Task.builder()
                .id(taskId)
                .title("Wrong Project")
                .projectId(otherProject.getId())
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(task);

        assertThrows(TaskNotFoundException.class, () ->
                taskService.deleteTask(adminUser, testProject.getId(), taskId)
        );

        verify(taskOutputPort, never()).deleteTaskById(anyLong());
    }


    @Test
    void updateTask_AsAdmin_Success() {
        long taskId = 200L;
        // existing task in DB
        Task existing = Task.builder()
                .id(taskId)
                .title("Old")
                .description("Old Desc")
                .projectId(testProject.getId())
                .status(TaskStatus.PENDING)
                .build();

        // incoming updated task
        Task updateDto = Task.builder()
                .title("New")
                .description("New Desc")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(existing);
        when(taskOutputPort.saveTask(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = taskService.updateTask(adminUser, testProject.getId(), taskId, updateDto);

        assertEquals("New", updated.getTitle());
        assertEquals("New Desc", updated.getDescription());
        assertEquals(TaskStatus.PENDING, updated.getStatus());
        verify(taskOutputPort).saveTask(any(Task.class));
    }

    @Test
    void updateTask_AsNonAdmin_ThrowsAccessDenied() {
        when(userOutputPort.getUserByEmail(regularUser.getEmail())).thenReturn(regularUser);

        Task dto = Task.builder().title("x").description("x").status(TaskStatus.PENDING).build();

        assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(regularUser, testProject.getId(), 1L, dto));
    }

    @Test
    void updateTask_TaskNotInProject_ThrowsNotFound() {
        long otherProjectId = 99L;
        Project other = Project.builder().id(otherProjectId).name("Other").build();
        Task existing = Task.builder()
                .id(300L)
                .title("Task")
                .projectId(otherProjectId)
                .build();

        Task dto = Task.builder().title("x").description("x").build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.getTaskById(300L)).thenReturn(existing);

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(adminUser, testProject.getId(), 300L, dto));

        verify(taskOutputPort, never()).saveTask(any());
    }
}
