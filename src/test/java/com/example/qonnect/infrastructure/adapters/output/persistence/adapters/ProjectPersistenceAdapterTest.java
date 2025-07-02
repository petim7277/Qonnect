package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.OrganizationPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.ProjectPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectPersistenceAdapterTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectPersistenceMapper projectPersistenceMapper;

    @Mock
    private OrganizationPersistenceMapper organizationPersistenceMapper;

    @InjectMocks
    private ProjectPersistenceAdapter adapter;

    private Project project;
    private ProjectEntity projectEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Organization org = new Organization();
        org.setId(1L);
        org.setName("Comma Hub");

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        user.setOrganization(org);

        project = Project.builder()
                .id(1L)
                .name("Qonnect")
                .description("Bug Tracker")
                .createdBy(user)
                .organization(org)
                .build();

        projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        projectEntity.setName("Qonnect");
    }

    @Test
    void shouldSaveProjectSuccessfully() {
        when(projectPersistenceMapper.toProjectEntity(project)).thenReturn(projectEntity);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectPersistenceMapper.toProject(projectEntity)).thenReturn(project);

        Project saved = adapter.saveProject(project);

        assertNotNull(saved);
        assertEquals("Qonnect", saved.getName());
        verify(projectRepository).save(projectEntity);
    }

    @Test
    void shouldReturnTrue_whenProjectExistsById() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        boolean exists = adapter.existById(1L);

        assertTrue(exists);
        verify(projectRepository).existsById(1L);
    }

    @Test
    void shouldReturnTrue_whenProjectExistsByNameAndOrganizationId() {
        when(projectRepository.existsProjectNameInOrganization("Qonnect", 1L)).thenReturn(true);

        boolean exists = adapter.existsByNameAndOrganizationId("Qonnect", 1L);

        assertTrue(exists);
        verify(projectRepository).existsProjectNameInOrganization("Qonnect", 1L);
    }
}
