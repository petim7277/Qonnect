package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.BugOutputPort;
import com.example.qonnect.application.output.OrganizationOutputPort;
import com.example.qonnect.application.output.ProjectOutputPort;
import com.example.qonnect.application.output.TaskOutputPort;
import com.example.qonnect.domain.exceptions.BugNotFoundException;
import com.example.qonnect.domain.exceptions.QonnectException;
import com.example.qonnect.domain.models.*;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BugServiceTest {

    @Mock
    private BugOutputPort bugOutputPort;
    @Mock private OrganizationOutputPort organizationOutputPort;
    @Mock private TaskOutputPort taskOutputPort;
    @Mock private ProjectOutputPort projectOutputPort;
    @Mock private BugRestMapper bugRestMapper;

    @InjectMocks
    private BugService bugService;

    private User user;
    private Task task;
    private Project project;
    private Bug bug;
    private Pageable pageable;

    @BeforeEach
    void setup() {
        Organization org = new Organization();
        org.setId(1L);

        user = new User();
        user.setId(1L);
        user.setEmail("user@qonnect.com");
        user.setOrganization(org);

        task = new Task();
        task.setId(10L);
        task.setProjectId(100L);

        project = new Project();
        project.setId(100L);
        project.setOrganizationId(org.getId());

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
    void getBugsByUserId_success() {
        Page<Bug> page = new PageImpl<>(List.of(bug));
        when(bugOutputPort.getBugsByUserId(user.getId(), pageable)).thenReturn(page);

        Page<Bug> result = bugService.getBugsByUserId(user.getId(), pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBugsByUserId_shouldThrow_whenUserIdNull() {
        assertThrows(QonnectException.class,
                () -> bugService.getBugsByUserId(null, pageable));
    }
}

