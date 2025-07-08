package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OrganizationRepository;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectPersistenceAdapterTest {

    @Autowired
    private ProjectPersistenceAdapter adapter;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization org;
    private User user;
    private Project project;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        OrganizationEntity orgEntity = OrganizationEntity.builder()
                .name("Comma Hub")
                .build();

        OrganizationEntity savedOrg = organizationRepository.save(orgEntity);

        org = Organization.builder()
                .id(savedOrg.getId())
                .name(savedOrg.getName())
                .build();


        user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        user.setOrganization(org);

        project = Project.builder()
                .name("Qonnect")
                .description("Bug Tracker")
                .createdById(user.getId())
                .organizationId(savedOrg.getId())
                .createdAt(LocalDateTime.now())
                .build();

        pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
    }

    @AfterEach
    void tearDown() {
        adapter.getAllProjects(org.getId(), pageable)
                .getContent()
                .forEach(adapter::deleteProject);

        organizationRepository.deleteById(org.getId());
    }

    @Test
    void shouldSaveProjectSuccessfully() {
        Project saved = adapter.saveProject(project);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Qonnect", saved.getName());
    }

    @Test
    void shouldReturnTrue_whenProjectExistsById() {
        Project saved = adapter.saveProject(project);

        boolean exists = adapter.existById(saved.getId());

        assertTrue(exists);
    }

    @Test
    void shouldReturnTrue_whenProjectExistsByNameAndOrganizationId() {
        Project saved = adapter.saveProject(project);

        boolean exists = adapter.existsByNameAndOrganizationId("Qonnect", project.getOrganizationId());

        assertTrue(exists);
    }

    @Test
    void shouldGetAllProjectsSuccessfully_whenProjectsExist() {
        Project project1 = Project.builder()
                .name("Project 1")
                .description("First project")
                .createdById(user.getId())
                .organizationId(org.getId())
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        Project project2 = Project.builder()
                .name("Project 2")
                .description("Second project")
                .createdById(user.getId())
                .organizationId(org.getId())
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        adapter.saveProject(project1);
        adapter.saveProject(project2);

        Page<Project> result = adapter.getAllProjects(org.getId(), pageable);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getContent().size() >= 2);

        boolean hasProject1 = result.getContent().stream()
                .anyMatch(p -> "Project 1".equals(p.getName()));
        boolean hasProject2 = result.getContent().stream()
                .anyMatch(p -> "Project 2".equals(p.getName()));

        assertTrue(hasProject1);
        assertTrue(hasProject2);
    }

    @Test
    void shouldReturnEmptyPage_whenNoProjectsExist() {
        Long nonExistentOrgId = 999999L;

        Page<Project> result = adapter.getAllProjects(nonExistentOrgId, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getProjectById_found() {
        Project saved = adapter.saveProject(project);

        Project result = adapter.getProjectById(saved.getId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals(saved.getName(), result.getName());
    }

    @Test
    void getProjectById_notFound() {
        Long invalidId = 999999L;

        assertThrows(ProjectNotFoundException.class, () -> adapter.getProjectById(invalidId));
    }



    @Test
    void shouldFindProjectById_whenProjectExists() {
        Project saved = adapter.saveProject(project);

        Optional<Project> result = adapter.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals("Qonnect", result.get().getName());
        assertEquals("Bug Tracker", result.get().getDescription());
        assertEquals(org.getId(), result.get().getOrganizationId());
    }

    @Test
    void shouldReturnEmpty_whenProjectNotFoundById() {
        Long nonExistentId = 999L;

        Optional<Project> result = adapter.findById(nonExistentId);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldDeleteProjectSuccessfully_whenProjectExists() {
        Project saved = adapter.saveProject(project);
        assertTrue(adapter.existById(saved.getId()));

        adapter.deleteProject(saved);

        assertFalse(adapter.existById(saved.getId()));
    }

    @Test
    void shouldThrowException_whenDeletingNullProject() {
        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> adapter.deleteProject(null));

        assertEquals(ErrorMessages.PROJECT_ID_IS_REQUIRED, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldThrowException_whenDeletingProjectWithNullId() {
        Project projectWithNullId = new Project();
        projectWithNullId.setName("Test Project");

        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> adapter.deleteProject(projectWithNullId));

        assertEquals(ErrorMessages.PROJECT_ID_IS_REQUIRED, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldThrowException_whenDeletingNonExistentProject() {

        Project nonExistentProject = new Project();
        nonExistentProject.setId(999L);
        nonExistentProject.setName("Non-existent Project");


        ProjectNotFoundException ex = assertThrows(ProjectNotFoundException.class,
                () -> adapter.deleteProject(nonExistentProject));

        assertEquals(ErrorMessages.PROJECT_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void shouldReturnTrue_whenProjectNameExistsExcludingCurrentProject() {
        Project project1 = Project.builder()
                .name("Existing Project")
                .description("First project")
                .createdById(user.getId())
                .organizationId(org.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Project project2 = Project.builder()
                .name("Another Project")
                .description("Second project")
                .createdById(user.getId())
                .organizationId(org.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Project saved1 = adapter.saveProject(project1);
        Project saved2 = adapter.saveProject(project2);


        boolean exists = adapter.existsByNameAndOrganizationIdAndNotId(
                "Existing Project", org.getId(), saved2.getId());


        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_whenProjectNameDoesNotExistExcludingCurrentProject() {
        Project saved = adapter.saveProject(project);

        boolean exists = adapter.existsByNameAndOrganizationIdAndNotId(
                "Qonnect", org.getId(), saved.getId());

        assertFalse(exists);
    }

    @Test
    void shouldReturnFalse_whenProjectNameDoesNotExistAtAll() {
        Project saved = adapter.saveProject(project);

        boolean exists = adapter.existsByNameAndOrganizationIdAndNotId(
                "Non-existent Project", org.getId(), saved.getId());

        assertFalse(exists);
    }

    @Test
    void shouldReturnTrue_whenProjectNameExistsInDifferentOrganization() {
        OrganizationEntity anotherOrgEntity = OrganizationEntity.builder()
                .name("Another Organization")
                .build();
        OrganizationEntity savedAnotherOrg = organizationRepository.save(anotherOrgEntity);

        Project projectInAnotherOrg = Project.builder()
                .name("Qonnect")
                .description("Same name in different org")
                .createdById(user.getId())
                .organizationId(savedAnotherOrg.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Project saved1 = adapter.saveProject(project);
        Project saved2 = adapter.saveProject(projectInAnotherOrg);


        boolean exists = adapter.existsByNameAndOrganizationIdAndNotId(
                "Qonnect", org.getId(), saved1.getId());


        assertFalse(exists);
    }

    @Test
    void shouldUpdateProjectSuccessfully_whenProjectExists() {
        Project saved = adapter.saveProject(project);

        saved.setName("Updated Project Name");
        saved.setDescription("Updated description");
        saved.setUpdatedAt(LocalDateTime.now());

        Project updated = adapter.saveProject(saved);

        assertNotNull(updated);
        assertEquals(saved.getId(), updated.getId());
        assertEquals("Updated Project Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(org.getId(), updated.getOrganizationId());
    }

    @Test
    void shouldThrowException_whenSavingProjectWithNonExistentOrganization() {

        project.setOrganizationId(999L);

        OrganizationNotFoundException ex = assertThrows(OrganizationNotFoundException.class,
                () -> adapter.saveProject(project));

        assertEquals(ErrorMessages.ORGANIZATION_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }


    @Test
    void shouldRemoveUserFromProjectSuccessfully() {
        Project savedProject = adapter.saveProject(project);

        User user1 = new User();
        user1.setId(100L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(101L);
        user2.setEmail("user2@example.com");

        savedProject.setTeamMembers(new ArrayList<>(List.of(user1, user2)));

        adapter.removeUserFromProject(user1, savedProject);

        Project updatedProject = adapter.getProjectById(savedProject.getId());

        assertNotNull(updatedProject);
        assertFalse(updatedProject.getTeamMembers().stream()
                .anyMatch(u -> u.getId().equals(user1.getId())));
    }

}
