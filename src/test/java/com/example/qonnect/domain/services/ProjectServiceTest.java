package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.ProjectAlreadyExistException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    @Mock
    private UserOutputPort userOutputPort;

    @Mock
    private ProjectOutputPort projectOutputPort;

    @InjectMocks
    private ProjectService projectService;

    private User adminUser;
    private Organization org;
    private Project project;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        org = new Organization();
        org.setId(1L);
        org.setName("TestOrg");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setOrganization(org);

        project = new Project();
        project.setName("New Project");
        project.setDescription("A test project");
    }

    @Test
    void shouldCreateProjectSuccessfully_whenValidAdminUser() {
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.existsByNameAndOrganizationId(project.getName(), org.getId())).thenReturn(false);
        when(projectOutputPort.saveProject(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.createProject(adminUser, project);

        assertNotNull(result);
        assertEquals(adminUser, result.getCreatedBy());
        assertEquals(org, result.getOrganization());
        assertEquals("New Project", result.getName());

        verify(projectOutputPort).saveProject(any(Project.class));
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        when(userOutputPort.existById(adminUser.getId())).thenReturn(false);

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> projectService.createProject(adminUser, project));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(projectOutputPort, never()).saveProject(any());
    }

    @Test
    void shouldThrowException_whenUserIsNotAdmin() {
        adminUser.setRole(Role.DEVELOPER);
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> projectService.createProject(adminUser, project));

        verify(projectOutputPort, never()).saveProject(any());
    }

    @Test
    void shouldThrowException_whenProjectAlreadyExists() {
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.existsByNameAndOrganizationId(project.getName(), org.getId())).thenReturn(true);

        assertThrows(ProjectAlreadyExistException.class,
                () -> projectService.createProject(adminUser, project));

        verify(projectOutputPort, never()).saveProject(any());
    }
}
