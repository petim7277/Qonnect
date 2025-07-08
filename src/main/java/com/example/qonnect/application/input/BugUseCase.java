package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.domain.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BugUseCase {
    Bug getBugById(User user, Long taskId, Long id);
    Bug updateBugDetails(User user,Long taskId,Bug bug);
    Bug updateBugStatus(User user,Long taskId,Bug bug);
    Bug updateBugSeverity(User user, Long taskId, Bug bug);
    Page<Bug> getAllBugsInAProject(User user,Long projectId, Pageable pageable);
    Page<Bug> getAllBugsInATask(User user,Long taskId, Pageable pageable);
    Page<Bug> getBugsByUserId(Long userId, Pageable pageable);
}
