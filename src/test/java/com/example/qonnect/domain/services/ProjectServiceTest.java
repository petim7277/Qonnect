package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.exceptions.ProjectAlreadyExistException;
import com.example.qonnect.domain.exceptions.ProjectException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private Pageable pageable;
    private List<Project> projectList;
    private Page<Project> projectPage;

    @BeforeEach
    void setUp() {

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

        pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("Project 1");
        project1.setDescription("First project");
        project1.setOrganizationId(org.getId());
        project1.setCreatedAt(LocalDateTime.now().minusDays(1));

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Project 2");
        project2.setDescription("Second project");
        project1.setOrganizationId(org.getId());
        project2.setCreatedAt(LocalDateTime.now());

        projectList = Arrays.asList(project1, project2);
        projectPage = new PageImpl<>(projectList, pageable, 2);
    }

    @Test
    void shouldCreateProjectSuccessfully_whenValidAdminUser() {
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.existsByNameAndOrganizationId(project.getName(), org.getId())).thenReturn(false);
        when(projectOutputPort.saveProject(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.createProject(adminUser, project);

        assertNotNull(result);
        assertEquals(adminUser.getId(), result.getCreatedById());
        assertEquals(org.getId(), result.getOrganizationId());
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


    @Test
    void shouldGetAllProjectsSuccessfully_whenValidOrganizationId() {
        Long organizationId = 1L;
        when(projectOutputPort.getAllProjects(organizationId, pageable)).thenReturn(projectPage);

        Page<Project> result = projectService.getAllProjects(organizationId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Project 1", result.getContent().get(0).getName());
        assertEquals("Project 2", result.getContent().get(1).getName());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());

        verify(projectOutputPort).getAllProjects(organizationId, pageable);
    }

    @Test
    void shouldReturnEmptyPage_whenNoProjectsFound() {
        Long organizationId = 1L;
        Page<Project> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(projectOutputPort.getAllProjects(organizationId, pageable)).thenReturn(emptyPage);

        Page<Project> result = projectService.getAllProjects(organizationId, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        assertTrue(result.getContent().isEmpty());

        verify(projectOutputPort).getAllProjects(organizationId, pageable);
    }

    @Test
    void shouldThrowException_whenOrganizationIdIsNull() {
        OrganizationNotFoundException ex = assertThrows(OrganizationNotFoundException.class,
                () -> projectService.getAllProjects(null, pageable));

        assertEquals(ErrorMessages.ORGANIZATION_NOT_FOUND, ex.getMessage());
        verify(projectOutputPort, never()).getAllProjects(any(), any());
    }





    @Test
    void shouldHandleDifferentPageSizes() {
        Long organizationId = 1L;
        Pageable customPageable = PageRequest.of(1, 5, Sort.by("name").ascending());
        Page<Project> customPage = new PageImpl<>(projectList, customPageable, 7);

        when(projectOutputPort.getAllProjects(organizationId, customPageable)).thenReturn(customPage);

        Page<Project> result = projectService.getAllProjects(organizationId, customPageable);

        assertNotNull(result);
        assertEquals(7, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());

        verify(projectOutputPort).getAllProjects(organizationId, customPageable);
    }

    @Test
    void shouldAssignUserToProject_successfully() {
        Long projectId = 1L;
        Long userId = 2L;

        User targetUser = new User();
        targetUser.setId(userId);
        targetUser.setOrganization(org);

        Project targetProject = new Project();
        targetProject.setId(projectId);
        targetProject.setOrganizationId(org.getId());
        targetProject.setTeamMembers(new ArrayList<>());

        when(projectOutputPort.getProjectById(projectId)).thenReturn(targetProject);
        when(userOutputPort.getUserById(userId)).thenReturn(targetUser);

        projectService.assignUserToProject(projectId, userId, adminUser);

        assertTrue(targetProject.getTeamMembers().contains(targetUser));
        verify(projectOutputPort).saveProject(targetProject);
    }

    @Test
    void shouldThrowException_whenAssigningUserWithDifferentOrg() {
        Long projectId = 1L;
        Long userId = 2L;

        Organization otherOrg = new Organization();
        otherOrg.setId(99L);

        User targetUser = new User();
        targetUser.setId(userId);
        targetUser.setOrganization(otherOrg);

        Project targetProject = new Project();
        targetProject.setId(projectId);
        targetProject.setOrganizationId(org.getId());
        targetProject.setTeamMembers(new ArrayList<>());

        when(projectOutputPort.getProjectById(projectId)).thenReturn(targetProject);
        when(userOutputPort.getUserById(userId)).thenReturn(targetUser);

        assertThrows(IllegalArgumentException.class, () ->
                projectService.assignUserToProject(projectId, userId, adminUser));
    }

    @Test
    void shouldThrowException_whenAssigningAlreadyAssignedUser() {
        Long projectId = 1L;
        Long userId = 2L;

        User targetUser = new User();
        targetUser.setId(userId);
        targetUser.setOrganization(org);

        Project targetProject = new Project();
        targetProject.setId(projectId);
        targetProject.setOrganizationId(org.getId());
        targetProject.setTeamMembers(new ArrayList<>(List.of(targetUser)));

        when(projectOutputPort.getProjectById(projectId)).thenReturn(targetProject);
        when(userOutputPort.getUserById(userId)).thenReturn(targetUser);

        assertThrows(ProjectException.class, () ->
                projectService.assignUserToProject(projectId, userId, adminUser));
    }

    @Test
    void shouldThrowException_whenAssigningUserAsNonAdmin() {
        adminUser.setRole(Role.DEVELOPER);

        assertThrows(AccessDeniedException.class, () ->
                projectService.assignUserToProject(1L, 2L, adminUser));
    }


}