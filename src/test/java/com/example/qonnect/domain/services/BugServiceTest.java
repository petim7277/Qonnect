package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.*;
import com.example.qonnect.domain.exceptions.BugNotFoundException;
import com.example.qonnect.domain.exceptions.QonnectException;
import com.example.qonnect.domain.models.*;
import com.example.qonnect.domain.models.enums.BugPriority;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.infrastructure.adapters.input.rest.mapper.BugRestMapper;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BugServiceTest {

    @Mock
    private BugOutputPort bugOutputPort;
    @Mock private OrganizationOutputPort organizationOutputPort;
    @Mock private TaskOutputPort taskOutputPort;
    @Mock private ProjectOutputPort projectOutputPort;
    @Mock private BugRestMapper bugRestMapper;
    @Mock private UserOutputPort userOutputPort;

    @InjectMocks
    private BugService bugService;

    private User user;
    private Task task;
    private User adminUser;
    private Project project;
    private Bug bug;
    private Pageable pageable;

    @BeforeEach
    void setup() {
        Organization org = Organization.builder()
                .id(1L)
                .build();


        user = new User();
        user.setId(1L);
        user.setEmail("user@qonnect.com");
        user.setRole(Role.QA_ENGINEER);
        user.setOrganization(org);

        task = new Task();
        task.setId(10L);
        task.setProjectId(100L);

        project = new Project();
        project.setId(100L);
        project.setOrganizationId(org.getId());

        adminUser= new User();
        adminUser = User.builder()
                .id(10L)
                .email("admin@qonnect.com")
                .role(Role.ADMIN)
                .organization(adminUser.getOrganization())
                .build();

        bug = new Bug();
        bug.setId(5L);
        bug.setTitle("Bug Title");
        bug.setDescription("Bug Desc");
        bug.setTaskId(task.getId());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getBugById_success() {
        when(taskOutputPort.getTaskById(task.getId())).thenReturn(task);
        when(projectOutputPort.getProjectById(task.getProjectId())).thenReturn(project);
        when(bugOutputPort.getBugByIdAndTaskId(bug.getId(), task.getId())).thenReturn(bug);

        Bug result = bugService.getBugById(user, task.getId(), bug.getId());

        assertEquals(bug.getId(), result.getId());
    }

    @Test
    void getBugById_shouldThrow_whenBugNotFound() {
        when(taskOutputPort.getTaskById(task.getId())).thenReturn(task);
        when(projectOutputPort.getProjectById(task.getProjectId())).thenReturn(project);
        when(bugOutputPort.getBugByIdAndTaskId(bug.getId(), task.getId())).thenReturn(null);

        assertThrows(BugNotFoundException.class,
                () -> bugService.getBugById(user, task.getId(), bug.getId()));
    }

    @Test
    void updateBugDetails_success() {
        Bug update = new Bug();
        update.setId(bug.getId());
        update.setTitle("Updated Title");
        update.setDescription("Updated Desc");

        when(taskOutputPort.getTaskById(task.getId())).thenReturn(task);
        when(projectOutputPort.getProjectById(task.getProjectId())).thenReturn(project);
        when(bugOutputPort.getBugByIdAndTaskId(update.getId(), task.getId())).thenReturn(bug);
        when(bugOutputPort.saveBug(any())).thenAnswer(inv -> inv.getArgument(0));

        Bug result = bugService.updateBugDetails(user, task.getId(), update);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());
    }

    @Test
    void updateBugDetails_shouldThrow_whenDescriptionMissing() {
        Bug invalid = new Bug();
        invalid.setId(bug.getId());

        assertThrows(QonnectException.class,
                () -> bugService.updateBugDetails(user, task.getId(), invalid));
    }

    @Test
    void updateBugStatus_success() {
        Bug update = new Bug();
        update.setId(bug.getId());
        update.setStatus(BugStatus.IN_PROGRESS);

        when(taskOutputPort.getTaskById(task.getId())).thenReturn(task);
        when(projectOutputPort.getProjectById(task.getProjectId())).thenReturn(project);
        when(bugOutputPort.getBugByIdAndTaskId(update.getId(), task.getId())).thenReturn(bug);
        when(bugOutputPort.saveBug(any())).thenAnswer(inv -> inv.getArgument(0));

        Bug result = bugService.updateBugStatus(user, task.getId(), update);

        assertEquals(BugStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void updateBugStatus_shouldThrow_whenStatusMissing() {
        Bug update = new Bug();
        update.setId(bug.getId());

        assertThrows(QonnectException.class,
                () -> bugService.updateBugStatus(user, task.getId(), update));
    }

    @Test
    void updateBugSeverity_success() {
        Bug update = new Bug();
        update.setId(bug.getId());
        update.setSeverity(BugSeverity.MAJOR);

        when(taskOutputPort.getTaskById(task.getId())).thenReturn(task);
        when(projectOutputPort.getProjectById(task.getProjectId())).thenReturn(project);
        when(bugOutputPort.getBugByIdAndTaskId(update.getId(), task.getId())).thenReturn(bug);
        when(bugOutputPort.saveBug(any())).thenAnswer(inv -> inv.getArgument(0));

        Bug result = bugService.updateBugSeverity(user, task.getId(), update);

        assertEquals(BugSeverity.MAJOR, result.getSeverity());
    }

    @Test
    void updateBugSeverity_shouldThrow_whenSeverityMissing() {
        Bug update = new Bug();
        update.setId(bug.getId());

        assertThrows(QonnectException.class,
                () -> bugService.updateBugSeverity(user, task.getId(), update));
    }

    @Test
    void getAllBugsInProject_success() {
        Page<Bug> page = new PageImpl<>(List.of(bug));

        when(projectOutputPort.getProjectById(project.getId())).thenReturn(project);
        when(bugOutputPort.getAllBugsByProjectId(project.getId(), pageable)).thenReturn(page);

        Page<Bug> result = bugService.getAllBugsInAProject(user, project.getId(), pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllBugsInTask_success() {
        Page<Bug> page = new PageImpl<>(List.of(bug));

        when(taskOutputPort.getTaskById(task.getId())).thenReturn(task);
        when(projectOutputPort.getProjectById(task.getProjectId())).thenReturn(project);
        when(bugOutputPort.getAllBugsByTaskId(task.getId(), pageable)).thenReturn(page);

        Page<Bug> result = bugService.getAllBugsInATask(user, task.getId(), pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBugsByAssignedToId_success() {
        Page<Bug> page = new PageImpl<>(List.of(bug));
        when(bugOutputPort.getBugsByUserId(user.getId(), pageable)).thenReturn(page);

        Page<Bug> result = bugService.getBugsByAssignedToId(user.getId(), pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBugsByUserId_shouldThrow_whenAssignedToIdNull() {
        assertThrows(QonnectException.class,
                () -> bugService.getBugsByAssignedToId(null, pageable));
    }


    @Test
    void testReportBug_Success() {
        Bug bug = Bug.builder()
                .title("Login button not working")
                .description("When clicked, it does nothing")
                .status(BugStatus.OPEN)
                .severity(BugSeverity.BLOCKER)
                .priority(BugPriority.HIGH)
                .projectId(project.getId())
                .build();

        when(projectOutputPort.getProjectById(project.getId())).thenReturn(project);
        when(bugOutputPort.existsByTitleAndProjectId(bug.getTitle(), bug.getProjectId())).thenReturn(false);
        when(bugOutputPort.saveBug(any(Bug.class))).thenAnswer(inv -> inv.getArgument(0));

        Bug saved = bugService.reportBug(user, bug);

        assertEquals(BugStatus.OPEN, saved.getStatus());
        assertEquals("Login button not working", saved.getTitle());
        assertEquals(user, saved.getCreatedBy());
    }

    @Test
    void testReportBug_UnauthorizedRole() {
        user.setRole(Role.DEVELOPER);

        Bug bug = Bug.builder()
                .title("Bug")
                .description("Desc")
                .status(BugStatus.OPEN)
                .severity(BugSeverity.MINOR)
                .priority(BugPriority.MEDIUM)
                .projectId(project.getId())
                .build();

        when(projectOutputPort.getProjectById(project.getId())).thenReturn(project);

        assertThrows(AccessDeniedException.class, () -> bugService.reportBug(user, bug));
    }

    @Test
    void testReportBug_DifferentOrganization() {
        project.setOrganizationId(99L);
        Bug bug = Bug.builder()
                .title("Bug")
                .description("Desc")
                .status(BugStatus.OPEN)
                .severity(BugSeverity.MAJOR)
                .priority(BugPriority.MEDIUM)
                .projectId(project.getId())
                .build();

        when(projectOutputPort.getProjectById(project.getId())).thenReturn(project);

        assertThrows(AccessDeniedException.class, () -> bugService.reportBug(user, bug));
    }

    @Test
    void testReportBug_TitleAlreadyExists() {
        Bug bug = Bug.builder()
                .title("Duplicate Bug")
                .description("Already logged")
                .status(BugStatus.OPEN)
                .severity(BugSeverity.MINOR)
                .priority(BugPriority.LOW)
                .projectId(project.getId())
                .build();

        when(projectOutputPort.getProjectById(project.getId())).thenReturn(project);
        when(bugOutputPort.existsByTitleAndProjectId(bug.getTitle(), project.getId())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> bugService.reportBug(user, bug));
    }

    @Test
    void testReportBug_TaskDoesNotBelongToProject() {
        Bug bug = Bug.builder()
                .title("Task mismatch")
                .description("The task belongs elsewhere")
                .status(BugStatus.OPEN)
                .severity(BugSeverity.MAJOR)
                .priority(BugPriority.MEDIUM)
                .projectId(project.getId())
                .taskId(999L)
                .build();

        Task wrongTask = Task.builder()
                .id(999L)
                .projectId(42L)
                .build();

        when(projectOutputPort.getProjectById(project.getId())).thenReturn(project);
        when(taskOutputPort.getTaskById(999L)).thenReturn(wrongTask);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bugService.reportBug(user, bug));

        assertEquals("Task does not belong to the specified project", ex.getMessage());
    }


    @Test
    void assignBugToDeveloper_success() {
        // Setup shared organization
        Organization org = Organization.builder().id(1L).build();

        // Assigner (admin)
        User assigner = User.builder()
                .id(1L)
                .email("admin@qonnect.com")
                .role(Role.ADMIN)
                .organization(org)
                .build();

        User developer = User.builder()
                .id(2L)
                .email("dev@example.com")
                .role(Role.DEVELOPER)
                .organization(org)
                .build();

        Bug bug = Bug.builder()
                .id(101L)
                .title("Critical Bug")
                .createdBy(assigner)
                .build();

        when(bugOutputPort.getBugById(bug.getId())).thenReturn(bug);
        when(userOutputPort.getUserById(developer.getId())).thenReturn(developer);
        when(bugOutputPort.saveBug(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Bug result = bugService.assignBugToDeveloper(assigner, bug.getId(), developer.getId());

        assertEquals(developer, result.getAssignedTo());
        verify(bugOutputPort).saveBug(bug);
    }



    @Test
    void assignBugToDeveloper_shouldThrow_ifAssignerNotAuthorized() {
        Organization org = Organization.builder().id(1L).build();

        adminUser = User.builder()
                .id(99L)
                .email("admin@qonnect.com")
                .role(Role.ADMIN)
                .organization(org)
                .build();

        User assigner = User.builder()
                .id(5L)
                .role(Role.DEVELOPER)
                .organization(org)
                .build();

        User developer = User.builder()
                .id(22L)
                .role(Role.DEVELOPER)
                .organization(org)
                .build();

        Bug bug = Bug.builder()
                .id(11L)
                .createdBy(adminUser)
                .build();

        when(bugOutputPort.getBugById(bug.getId())).thenReturn(bug);
        when(userOutputPort.getUserById(developer.getId())).thenReturn(developer);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                bugService.assignBugToDeveloper(assigner, bug.getId(), developer.getId()));

        assertEquals("Only Admins or QA Engineers can assign bugs", ex.getMessage());
    }


    @Test
    void assignBugToDeveloper_shouldThrow_ifAssignedUserIsNotDeveloper() {
        Organization org = Organization.builder().id(1L).build();

        User assigner = User.builder()
                .id(5L)
                .role(Role.ADMIN)
                .organization(org)
                .build();

        Bug bug = Bug.builder()
                .id(30L)
                .createdBy(assigner)
                .build();

        User invalidAssignee = User.builder()
                .id(55L)
                .role(Role.QA_ENGINEER)
                .organization(org)
                .build();

        when(bugOutputPort.getBugById(bug.getId())).thenReturn(bug);
        when(userOutputPort.getUserById(invalidAssignee.getId())).thenReturn(invalidAssignee);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bugService.assignBugToDeveloper(assigner, bug.getId(), invalidAssignee.getId()));

        assertEquals("Assigned user must be a developer", ex.getMessage());
    }


    @Test
    void assignBugToDeveloper_shouldThrow_ifUserFromDifferentOrg() {
        Organization adminOrg = Organization.builder().id(1L).build();
        adminUser = User.builder()
                .id(10L)
                .role(Role.ADMIN)
                .organization(adminOrg)
                .build();

        Organization otherOrg = Organization.builder().id(99L).build();

        User developer = User.builder()
                .id(77L)
                .role(Role.DEVELOPER)
                .organization(otherOrg)
                .build();

        Bug bug = Bug.builder()
                .id(50L)
                .createdBy(adminUser)
                .build();

        when(bugOutputPort.getBugById(bug.getId())).thenReturn(bug);
        when(userOutputPort.getUserById(developer.getId())).thenReturn(developer);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                bugService.assignBugToDeveloper(adminUser, bug.getId(), developer.getId()));

        assertEquals("You don't belong to this organization", ex.getMessage());
    }




}

