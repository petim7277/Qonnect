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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectPersistenceAdapterIntegrationTest {

    @Autowired
    private ProjectPersistenceAdapter adapter;


    private Project project;

    @BeforeEach
    void setUp() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Comma Hub");

        User user = new User();
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
}
