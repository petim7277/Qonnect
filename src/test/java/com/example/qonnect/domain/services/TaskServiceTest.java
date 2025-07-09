package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.TaskAlreadyAssignedException;
import com.example.qonnect.domain.exceptions.TaskAlreadyExistException;
import com.example.qonnect.domain.exceptions.TaskNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

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


        Organization testOrg = Organization.builder()
                .id(1L)
                .name("TestOrg")
                .build();

        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .organization(testOrg)
                .build();

        regularUser = User.builder()
                .id(2L)
                .email("user@example.com")
                .organization(testOrg)
                .role(Role.DEVELOPER)
                .build();

        testProject = Project.builder()
                .id(10L)
                .organizationId(testOrg.getId())
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
        Task existing = Task.builder()
                .id(taskId)
                .title("Old")
                .description("Old Desc")
                .projectId(testProject.getId())
                .status(TaskStatus.PENDING)
                .build();

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


    @Test
    void getAllTasksInProject_Success() {
        Long projectId = testProject.getId();
        List<Task> mockTasks = List.of(
                Task.builder().id(1L).title("Task 1").projectId(projectId).build(),
                Task.builder().id(2L).title("Task 2").projectId(projectId).build()
        );

        when(projectOutputPort.getProjectById(projectId)).thenReturn(testProject);
        when(taskOutputPort.getAllTasksByProjectId(projectId)).thenReturn(mockTasks);

        List<Task> result = taskService.getAllTasksInProject(adminUser, projectId);

        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());

        verify(projectOutputPort).getProjectById(projectId);
        verify(taskOutputPort).getAllTasksByProjectId(projectId);
    }

    @Test
    void getAllTasksInProject_ProjectNotFound_ThrowsException() {
        Long nonExistentProjectId = 999L;

        when(projectOutputPort.getProjectById(nonExistentProjectId))
                .thenThrow(new TaskNotFoundException("Project not found", org.springframework.http.HttpStatus.NOT_FOUND));

        assertThrows(TaskNotFoundException.class, () ->
                taskService.getAllTasksInProject(adminUser, nonExistentProjectId)
        );

        verify(projectOutputPort).getProjectById(nonExistentProjectId);
        verify(taskOutputPort, never()).getAllTasksByProjectId(anyLong());
    }

    @Test
    void viewTaskInProject_AsTeamMember_Success() {
        Long taskId = 301L;
        Task task = Task.builder()
                .id(taskId)
                .title("Bug Fix")
                .projectId(testProject.getId())
                .build();

        // Add user as a team member of the project
        testProject.setTeamMembers(List.of(regularUser));

        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(task);

        Task result = taskService.viewTaskInProject(regularUser, testProject.getId(), taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Bug Fix", result.getTitle());

        verify(projectOutputPort).getProjectById(testProject.getId());
        verify(taskOutputPort).getTaskById(taskId);
    }

    @Test
    void viewTaskInProject_NotTeamMember_ThrowsAccessDenied() {
        Long taskId = 302L;

        testProject.setTeamMembers(List.of(adminUser));

        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);

        assertThrows(AccessDeniedException.class, () -> {
            taskService.viewTaskInProject(regularUser, testProject.getId(), taskId);
        });

        verify(projectOutputPort).getProjectById(testProject.getId());
        verify(taskOutputPort, never()).getTaskById(anyLong());
    }

    @Test
    void viewTaskInProject_TaskNotInProject_Throws() {
        Long taskId = 303L;

        testProject.setTeamMembers(List.of(regularUser));

        Task task = Task.builder()
                .id(taskId)
                .title("Outside Task")
                .projectId(999L)
                .build();

        when(projectOutputPort.getProjectById(testProject.getId())).thenReturn(testProject);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(task);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.viewTaskInProject(regularUser, testProject.getId(), taskId);
        });

        verify(projectOutputPort).getProjectById(testProject.getId());
        verify(taskOutputPort).getTaskById(taskId);
    }

    @Test
    void assignTaskToUser_AsAdmin_Success() {
        Long taskId = 400L;
        Long userId = 3L;

        Organization org = Organization.builder().id(1L).name("Org1").build();
        adminUser.setOrganization(org);

        User developer = User.builder()
                .id(userId)
                .role(Role.DEVELOPER)
                .organization(org)
                .build();

        Task task = Task.builder()
                .id(taskId)
                .title("Assign Me")
                .projectId(10L)
                .build();

        Project project = Project.builder()
                .id(10L)
                .organizationId(org.getId())
                .build();

        when(userOutputPort.getUserByEmail(adminUser.getEmail())).thenReturn(adminUser);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(task);
        when(projectOutputPort.getProjectById(10L)).thenReturn(project);
        when(userOutputPort.getUserById(userId)).thenReturn(developer);
        when(taskOutputPort.saveTask(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.assignTaskToUser(adminUser, taskId, userId);

        assertNotNull(result.getAssignedTo());
        assertEquals(userId, result.getAssignedTo().getId());
        verify(taskOutputPort).saveTask(any(Task.class));
    }

    @Test
    void selfAssignTask_Success() {
        Long taskId = 401L;

        Organization org = Organization.builder().id(1L).build();
        regularUser.setOrganization(org);

        Task task = Task.builder()
                .id(taskId)
                .title("Unassigned Task")
                .projectId(10L)
                .build();

        Project project = Project.builder()
                .id(10L)
                .organizationId(org.getId())
                .build();

        when(userOutputPort.getUserByEmail(regularUser.getEmail())).thenReturn(regularUser);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(task);
        when(projectOutputPort.getProjectById(10L)).thenReturn(project);
        when(taskOutputPort.saveTask(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.selfAssignTask(regularUser, taskId);

        assertNotNull(result.getAssignedTo());
        assertEquals(regularUser.getId(), result.getAssignedTo().getId());
    }

    @Test
    void selfAssignTask_AlreadyAssigned_Throws() {
        Long taskId = 402L;

        Organization org = Organization.builder().id(1L).build();
        regularUser.setOrganization(org);

        User anotherDeveloper = User.builder()
                .id(99L)
                .email("other@example.com")
                .role(Role.DEVELOPER)
                .organization(org)
                .build();

        Task task = Task.builder()
                .id(taskId)
                .title("Already Taken")
                .projectId(10L)
                .assignedTo(anotherDeveloper)
                .build();

        Project project = Project.builder()
                .id(10L)
                .organizationId(org.getId())
                .build();

        when(userOutputPort.getUserByEmail(regularUser.getEmail())).thenReturn(regularUser);
        when(taskOutputPort.getTaskById(taskId)).thenReturn(task);
        when(projectOutputPort.getProjectById(10L)).thenReturn(project);

        assertThrows(TaskAlreadyAssignedException.class, () ->
                taskService.selfAssignTask(regularUser, taskId));
    }


    @Test
    void testGetTasksByUserId_Success() {
        Long userId = 2L;
        Pageable pageable = PageRequest.of(0, 5);

        Task task1 = Task.builder()
                .id(1L)
                .title("Task 1")
                .assignedTo(regularUser)
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .title("Task 2")
                .assignedTo(regularUser)
                .build();

        Page<Task> taskPage = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(taskOutputPort.getTasksByUserId(userId, pageable)).thenReturn(taskPage);

        Page<Task> result = taskService.getTasksByUserId(userId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Task 1", result.getContent().get(0).getTitle());
        assertEquals("Task 2", result.getContent().get(1).getTitle());

        verify(taskOutputPort).getTasksByUserId(userId, pageable);
    }




}
