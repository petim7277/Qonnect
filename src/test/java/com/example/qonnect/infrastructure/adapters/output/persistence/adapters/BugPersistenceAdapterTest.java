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
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;

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
    @Autowired
    private ProjectOutputPort projectOutputPort;
    @Autowired
    private TaskOutputPort taskOutputPort;
    @Autowired
    private OrganizationOutputPort organizationOutputPort;
    @Autowired
    private OrganizationRepository organizationRepository;

    private Bug bug;
    private Pageable pageable;
    private User createdBy;
    private User assignedTo;
    private Project project;
    private Task task;
    private Organization organization;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        organization = organizationOutputPort.saveOrganization(
                Organization.builder().name("Test Org").build()
        );

        project = projectOutputPort.saveProject(
                Project.builder().name("Bug Project").organizationId(organization.getId()).build()
        );

        task = taskOutputPort.saveTask(
                Task.builder().title("Task for Bug").projectId(project.getId()).build()
        );

        createdBy = userOutputPort.saveUser(
                User.builder()
                        .firstName("Test")
                        .lastName("User")
                        .email("testuser3@example.com")
                        .password("password")
                        .enabled(true)
                        .invited(false)
                        .role(Role.DEVELOPER)
                        .organization(organization)
                        .build()
        );

         assignedTo = userOutputPort.saveUser(
                User.builder()
                        .firstName("Dev")
                        .lastName("Assignee")
                        .email("assigneduser@example.com")
                        .password("password")
                        .enabled(true)
                        .invited(false)
                        .role(Role.DEVELOPER)
                        .organization(organization)
                        .build()
        );


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

        if (task != null) taskRepository.deleteById(task.getId());
        if (project != null) projectRepository.deleteById(project.getId());
        if (organization != null) organizationRepository.deleteById(organization.getId());

        if (createdBy != null && userRepository.existsById(createdBy.getId())) {
            userRepository.deleteById(createdBy.getId());
        }
        if (assignedTo != null && userRepository.existsById(assignedTo.getId())) {
            userRepository.deleteById(assignedTo.getId());
        }

    }


    @Test
    void shouldSaveBugSuccessfully() {
        Bug saved = adapter.saveBug(bug);
        assertNotNull(saved.getId());
        assertEquals("Adapter Test Bug", saved.getTitle());

        Optional<?> found = bugRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void shouldRetrieveBugByIdAndTaskId() {
        Bug saved = adapter.saveBug(bug);
        Bug result = adapter.getBugByIdAndTaskId(saved.getId(), saved.getTaskId());
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void shouldThrow_whenBugNotFoundByIdAndTaskId() {
        assertThrows(BugNotFoundException.class,
                () -> adapter.getBugByIdAndTaskId(999L, 999L));
    }

    @Test
    void shouldGetAllBugsByProjectId() {
        adapter.saveBug(bug);
        adapter.saveBug(Bug.builder()
                .title("Another Bug")
                .description("Different bug")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.MINOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build());

        Page<Bug> result = adapter.getAllBugsByProjectId(project.getId(), pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldGetAllBugsByTaskId() {
        adapter.saveBug(bug);
        adapter.saveBug(Bug.builder()
                .title("Task Bug 2")
                .description("2nd bug")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.MINOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build());

        Page<Bug> result = adapter.getAllBugsByTaskId(task.getId(), pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldGetBugsByUserId() {
        Bug bug1 = Bug.builder()
                .title("Assigned Bug")
                .description("Bug assigned to user")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .assignedTo(assignedTo)
                .severity(BugSeverity.MAJOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        Bug bug2 = Bug.builder()
                .title("User Bug")
                .description("Owned by same user")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .assignedTo(assignedTo)
                .severity(BugSeverity.MAJOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        adapter.saveBug(bug1);
        adapter.saveBug(bug2);

        Page<Bug> result = adapter.getBugsByUserId(assignedTo.getId(), pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldGetBugsByCreatedById() {
        Bug bug1 = Bug.builder()
                .title("Created Bug 1")
                .description("Created by user")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.MINOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        Bug bug2 = Bug.builder()
                .title("Created Bug 2")
                .description("Another one")
                .taskId(task.getId())
                .projectId(project.getId())
                .createdBy(createdBy)
                .severity(BugSeverity.MAJOR)
                .status(BugStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        adapter.saveBug(bug1);
        adapter.saveBug(bug2);

        Page<Bug> result = adapter.getBugsByCreatedById(createdBy.getId(), pageable);
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
        assertThrows(BugNotFoundException.class, () -> adapter.deleteBug(12345L));
    }


    @Test
    void shouldReturnTrue_whenBugExistsByTitleAndProjectId() {
        adapter.saveBug(bug);
        assertTrue(adapter.existsByTitleAndProjectId(bug.getTitle(), project.getId()));
    }

    @Test
    void shouldReturnFalse_whenBugDoesNotExistByTitleAndProjectId() {
        assertFalse(adapter.existsByTitleAndProjectId("Unknown Bug", project.getId()));
    }
}