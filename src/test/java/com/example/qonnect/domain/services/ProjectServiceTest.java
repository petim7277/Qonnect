package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.exceptions.ProjectAlreadyExistException;
import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
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
import java.util.Optional;

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

    private User developerUser;
    private Project existingProject;

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


        developerUser = new User();
        developerUser.setId(2L);
        developerUser.setEmail("developer@example.com");
        developerUser.setRole(Role.DEVELOPER);
        developerUser.setOrganization(org);

        existingProject = new Project();
        existingProject.setId(1L);
        existingProject.setName("Existing Project");
        existingProject.setDescription("An existing project");
        existingProject.setOrganizationId(org.getId());
        existingProject.setCreatedById(adminUser.getId());
        existingProject.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingProject.setUpdatedAt(LocalDateTime.now().minusDays(1));
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



    @Test
    void shouldGetProjectById_whenValidAdminUser() {
        Long projectId = 1L;
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));

        Project result = projectService.getProjectById(adminUser, projectId);

        assertNotNull(result);
        assertEquals(existingProject.getId(), result.getId());
        assertEquals(existingProject.getName(), result.getName());
        verify(projectOutputPort).findById(projectId);
    }

    @Test
    void shouldGetProjectById_whenValidDeveloperUser() {
        Long projectId = 1L;
        when(userOutputPort.existById(developerUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));

        Project result = projectService.getProjectById(developerUser, projectId);

        assertNotNull(result);
        assertEquals(existingProject.getId(), result.getId());
        verify(projectOutputPort).findById(projectId);
    }

    @Test
    void shouldThrowException_whenGetProjectByIdWithNullProjectId() {
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);

        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> projectService.getProjectById(adminUser, null));

        assertEquals("Project ID is required", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(projectOutputPort, never()).findById(any());
    }

    @Test
    void shouldThrowException_whenGetProjectByIdWithNonExistentProject() {
        Long projectId = 999L;
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.empty());

        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> projectService.getProjectById(adminUser, projectId));

        assertEquals(ErrorMessages.PROJECT_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void shouldThrowException_whenGetProjectByIdWithUserFromDifferentOrganization() {
        Long projectId = 1L;
        Organization differentOrg = new Organization();
        differentOrg.setId(2L);
        differentOrg.setName("Different Org");

        User userFromDifferentOrg = new User();
        userFromDifferentOrg.setId(3L);
        userFromDifferentOrg.setEmail("other@example.com");
        userFromDifferentOrg.setRole(Role.ADMIN);
        userFromDifferentOrg.setOrganization(differentOrg);

        when(userOutputPort.existById(userFromDifferentOrg.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> projectService.getProjectById(userFromDifferentOrg, projectId));

        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
    }

    @Test
    void shouldThrowException_whenGetProjectByIdWithNonExistentUser() {
        Long projectId = 1L;
        when(userOutputPort.existById(adminUser.getId())).thenReturn(false);

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> projectService.getProjectById(adminUser, projectId));

        assertEquals(ErrorMessages.USER_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }


    @Test
    void shouldUpdateProject_whenValidAdminUser() {
        Long projectId = 1L;
        Project updatedProject = new Project();
        updatedProject.setName("Updated Project Name");
        updatedProject.setDescription("Updated description");

        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(projectOutputPort.existsByNameAndOrganizationIdAndNotId(
                "Updated Project Name", org.getId(), projectId)).thenReturn(false);
        when(projectOutputPort.saveProject(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.updateProject(adminUser, projectId, updatedProject);

        assertNotNull(result);
        assertEquals("Updated Project Name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(org.getId(), result.getOrganizationId());
        assertNotNull(result.getUpdatedAt());
        verify(projectOutputPort).saveProject(any(Project.class));
    }

    @Test
    void shouldThrowException_whenUpdateProjectWithDeveloperUser() {
        Long projectId = 1L;
        Project updatedProject = new Project();
        updatedProject.setName("Updated Name");
        updatedProject.setDescription("Updated description");

        when(userOutputPort.existById(developerUser.getId())).thenReturn(true);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> projectService.updateProject(developerUser, projectId, updatedProject));

        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
        verify(projectOutputPort, never()).saveProject(any());
    }

    @Test
    void shouldThrowException_whenUpdateProjectWithDuplicateName() {
        Long projectId = 1L;
        Project updatedProject = new Project();
        updatedProject.setName("Duplicate Name");
        updatedProject.setDescription("Updated description");

        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(projectOutputPort.existsByNameAndOrganizationIdAndNotId(
                "Duplicate Name", org.getId(), projectId)).thenReturn(true);

        ProjectAlreadyExistException ex = assertThrows(ProjectAlreadyExistException.class,
                () -> projectService.updateProject(adminUser, projectId, updatedProject));

        assertEquals(ErrorMessages.PROJECT_EXIST_ALREADY, ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(projectOutputPort, never()).saveProject(any());
    }

    @Test
    void shouldThrowException_whenUpdateProjectWithNullProjectId() {
        Project updatedProject = new Project();
        updatedProject.setName("Updated Name");
        updatedProject.setDescription("Updated description");

        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);

        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> projectService.updateProject(adminUser, null, updatedProject));

        assertEquals("Project ID is required", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldThrowException_whenUpdateProjectWithNonExistentProject() {
        Long projectId = 999L;
        Project updatedProject = new Project();
        updatedProject.setName("Updated Name");
        updatedProject.setDescription("Updated description");

        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.empty());

        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> projectService.updateProject(adminUser, projectId, updatedProject));

        assertEquals(ErrorMessages.PROJECT_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }


    @Test
    void shouldDeleteProject_whenValidAdminUser() {
        Long projectId = 1L;
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));
        doNothing().when(projectOutputPort).deleteProject(existingProject);

        projectService.deleteProject(adminUser, projectId);

        verify(projectOutputPort).deleteProject(existingProject);
    }

    @Test
    void shouldThrowException_whenDeleteProjectWithDeveloperUser() {
        Long projectId = 1L;
        when(userOutputPort.existById(developerUser.getId())).thenReturn(true);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> projectService.deleteProject(developerUser, projectId));

        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
        verify(projectOutputPort, never()).deleteProject(any());
    }

    @Test
    void shouldThrowException_whenDeleteProjectWithNullProjectId() {
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);

        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> projectService.deleteProject(adminUser, null));

        assertEquals("Project ID is required", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldThrowException_whenDeleteProjectWithNonExistentProject() {
        Long projectId = 999L;
        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.empty());

        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> projectService.deleteProject(adminUser, projectId));

        assertEquals(ErrorMessages.PROJECT_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void shouldThrowException_whenDeleteProjectWithUserFromDifferentOrganization() {
        Long projectId = 1L;
        Organization differentOrg = new Organization();
        differentOrg.setId(2L);
        differentOrg.setName("Different Org");

        User userFromDifferentOrg = new User();
        userFromDifferentOrg.setId(3L);
        userFromDifferentOrg.setEmail("other@example.com");
        userFromDifferentOrg.setRole(Role.ADMIN);
        userFromDifferentOrg.setOrganization(differentOrg);

        when(userOutputPort.existById(userFromDifferentOrg.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> projectService.deleteProject(userFromDifferentOrg, projectId));

        assertEquals(ErrorMessages.ACCESS_DENIED, ex.getMessage());
        verify(projectOutputPort, never()).deleteProject(any());
    }


    @Test
    void shouldThrowException_whenUpdateProjectWithInvalidName() {

        Long projectId = 1L;
        Project updatedProject = new Project();
        updatedProject.setName("");
        updatedProject.setDescription("Valid description");

        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> projectService.updateProject(adminUser, projectId, updatedProject));


        verify(projectOutputPort, never()).saveProject(any());
    }

    @Test
    void shouldThrowException_whenUpdateProjectWithInvalidDescription() {

        Long projectId = 1L;
        Project updatedProject = new Project();
        updatedProject.setName("Valid Name");
        updatedProject.setDescription("");

        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> projectService.updateProject(adminUser, projectId, updatedProject));

        verify(projectOutputPort, never()).saveProject(any());
    }

    @Test
    void shouldThrowException_whenUserOrganizationIsNull() {
        Long projectId = 1L;
        adminUser.setOrganization(null);

        when(userOutputPort.existById(adminUser.getId())).thenReturn(true);
        when(projectOutputPort.findById(projectId)).thenReturn(Optional.of(existingProject));

        OrganizationNotFoundException ex = assertThrows(OrganizationNotFoundException.class,
                () -> projectService.getProjectById(adminUser, projectId));

        assertEquals(ErrorMessages.ORGANIZATION_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }




}