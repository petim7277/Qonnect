package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectPersistenceAdapterIntegrationTest {

    @Autowired
    private ProjectPersistenceAdapter adapter;


    private Organization org;
    private User user;
    private Project project;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
       org = new Organization();
        org.setId(1L);
        org.setName("Comma Hub");

        user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        user.setOrganization(org);

        project = Project.builder()
                .name("Qonnect")
                .description("Bug Tracker")
                .createdBy(user)
                .organization(org)
                .createdAt(LocalDateTime.now())
                .build();

        pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
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

        boolean exists = adapter.existsByNameAndOrganizationId("Qonnect", project.getOrganization().getId());

        assertTrue(exists);
    }


    @Test
    void shouldGetAllProjectsSuccessfully_whenProjectsExist() {
        // Given - Save multiple projects for the same organization
        Project project1 = Project.builder()
                .name("Project 1")
                .description("First project")
                .createdBy(user)
                .organization(org)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        Project project2 = Project.builder()
                .name("Project 2")
                .description("Second project")
                .createdBy(user)
                .organization(org)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        adapter.saveProject(project1);
        adapter.saveProject(project2);

        // When
        Page<Project> result = adapter.getAllProjects(org.getId(), pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getContent().size() >= 2);
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());

        // Verify projects are returned (order may vary based on creation time)
        boolean hasProject1 = result.getContent().stream()
                .anyMatch(p -> "Project 1".equals(p.getName()));
        boolean hasProject2 = result.getContent().stream()
                .anyMatch(p -> "Project 2".equals(p.getName()));

        assertTrue(hasProject1);
        assertTrue(hasProject2);
    }

    @Test
    void shouldReturnEmptyPage_whenNoProjectsExist() {
        // Given - Using a non-existent organization ID
        Long nonExistentOrgId = 999L;

        // When
        Page<Project> result = adapter.getAllProjects(nonExistentOrgId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
    }
}
