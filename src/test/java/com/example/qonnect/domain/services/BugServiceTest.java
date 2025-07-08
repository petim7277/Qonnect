package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.*;
import com.example.qonnect.domain.exceptions.BugNotFoundException;
import com.example.qonnect.domain.exceptions.QonnectException;
import com.example.qonnect.domain.models.*;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.BugRestMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.adapters.BugPersistenceAdapter;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.BugRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class BugPersistenceAdapterTest {

    @Autowired
    private BugPersistenceAdapter adapter;
    @Autowired private BugRepository bugRepository;
    @Autowired private UserOutputPort userOutputPort;
    @Autowired private ProjectOutputPort projectOutputPort;
    @Autowired private TaskOutputPort taskOutputPort;
    @Autowired private OrganizationOutputPort organizationOutputPort;

    private Bug bug;
    private User createdBy;
    private Project project;
    private Task task;
    private Organization organization;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        createdBy = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("testuser3@example.com")
                .password("password")
                .enabled(true)
                .invited(false)
                .role(Role.DEVELOPER)
                .build();
        createdBy = userOutputPort.saveUser(createdBy);

        organization = new Organization();
        organization.setName("Test Org");
        organization = organizationOutputPort.saveOrganization(organization);

        project = Project.builder()
                .name("naming")
                .organizationId(organization.getId())
                .build();
        project = projectOutputPort.saveProject(project);

        task = Task.builder()
                .title("naming")
                .projectId(project.getId())
                .build();
        task = taskOutputPort.saveTask(task);

        bug = Bug.builder()
                .title("Adapter Test Bug")
                .description("Bug created for adapter tests")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.MAJOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        bugRepository.deleteAll();

        if (task != null && task.getId() != null) {
            taskOutputPort.deleteTaskById(task.getId());
        }

        if (project != null && project.getId() != null) {
            projectOutputPort.deleteProjectById(project.getId());
        }

        if (createdBy != null && createdBy.getId() != null) {
            userOutputPort.deleteUserById(createdBy.getId());
        }

        if (organization != null && organization.getId() != null) {
            organizationOutputPort.deleteOrganizationById(organization.getId());
        }
    }

    @Test
    void shouldSaveBugSuccessfully() {
        Bug saved = adapter.saveBug(bug);

        assertNotNull(saved.getId());
        assertEquals("Adapter Test Bug", saved.getTitle());

        Optional<BugEntity> found = bugRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void shouldRetrieveBugByIdAndTaskId() {
        Bug saved = adapter.saveBug(bug);

        Bug result = adapter.getBugByIdAndTaskId(saved.getId(), saved.getTaskId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals(saved.getTaskId(), result.getTaskId());
    }

    @Test
    void shouldThrow_whenBugNotFoundByIdAndTaskId() {
        assertThrows(BugNotFoundException.class,
                () -> adapter.getBugByIdAndTaskId(999L, 10L));
    }

    @Test
    void shouldGetAllBugsByProjectId() {
        adapter.saveBug(bug);

        Bug secondBug = Bug.builder()
                .title("Another Bug")
                .description("Second bug")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.MINOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        adapter.saveBug(secondBug);

        Page<Bug> result = adapter.getAllBugsByProjectId(project.getId(), pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Another Bug", result.getContent().get(0).getTitle());
    }

    @Test
    void shouldGetAllBugsByTaskId() {
        adapter.saveBug(bug);

        Bug secondBug = Bug.builder()
                .title("Task Bug")
                .description("Task-related bug")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.CRITICAL)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        adapter.saveBug(secondBug);

        Page<Bug> result = adapter.getAllBugsByTaskId(task.getId(), pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldGetBugsByUserId() {
        adapter.saveBug(bug);

        Bug anotherBug = Bug.builder()
                .title("User Bug")
                .description("Another bug by user")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.MAJOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        adapter.saveBug(anotherBug);

        Page<Bug> result = adapter.getBugsByUserId(createdBy.getId(), pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldReturnTrue_whenBugExistsById() {
        Bug saved = adapter.saveBug(bug);
        assertTrue(adapter.existsById(saved.getId()));
    }

    @Test
    void shouldReturnFalse_whenBugDoesNotExistById() {
        assertFalse(adapter.existsById(999999L));
    }

    @Test
    void shouldDeleteBugSuccessfully() {
        Bug saved = adapter.saveBug(bug);
        adapter.deleteBug(saved.getId());
        assertFalse(bugRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldThrow_whenDeletingNonExistingBug() {
        assertThrows(BugNotFoundException.class,
                () -> adapter.deleteBug(12345L));
    }
}
