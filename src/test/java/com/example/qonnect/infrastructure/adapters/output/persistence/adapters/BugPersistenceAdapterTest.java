package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.OrganizationOutputPort;
import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.BugNotFoundException;
import com.example.qonnect.domain.models.*;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.BugEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.BugRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BugPersistenceAdapterTest {

    @Autowired
    private BugPersistenceAdapter adapter;
    @Autowired
    private BugRepository bugRepository;
    @Autowired
    private UserOutputPort userOutputPort;
    private Bug bug;
    private Pageable pageable;
    private User createdBy;

    @Autowired
    private ProjectOutputPort projectOutputPort;

    private Project project;
    private Task task;
    @Autowired
    private TaskOutputPort taskOutputPort;
    @Autowired private OrganizationOutputPort organizationOutputPort;
    private Organization organization;

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

        organization =  Organization.builder()
                .name("Test Org")
                .build();
        organizationOutputPort.saveOrganization(organization);

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
        userOutputPort.deleteUserById(createdBy.getId());
        projectOutputPort.deleteProject(project);
        taskOutputPort.deleteTaskById(task.getId());
        bugRepository.deleteAll();

        organizationOutputPort.deleteById(organization.getId());
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
        bug.setTitle("Another Bug");
        adapter.saveBug(bug);

        Page<Bug> result = adapter.getAllBugsByProjectId(20L, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Another Bug", result.getContent().get(0).getTitle());
    }

    @Test
    void shouldGetAllBugsByTaskId() {
        adapter.saveBug(bug);
        bug.setTitle("Task Bug");
        adapter.saveBug(bug);

        Page<Bug> result = adapter.getAllBugsByTaskId(10L, pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldGetBugsByUserId() {
        adapter.saveBug(bug);
        bug.setTitle("User Bug");
        adapter.saveBug(bug);

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
        assertFalse(adapter.existsById(7777L));
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
