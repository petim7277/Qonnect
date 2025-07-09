package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Bug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BugOutputPort {

    Bug getBugByIdAndTaskId(Long id, Long taskId);


    Bug saveBug(Bug bug);

    Page<Bug> getAllBugsByProjectId(Long projectId, Pageable pageable);

    Page<Bug> getAllBugsByTaskId(Long taskId, Pageable pageable);

    Page<Bug> getBugsByUserId(Long userId, Pageable pageable);

    boolean existsById(Long id);

    void deleteBug(Long id);

    boolean existsByTitleAndProjectId(String title, Long projectId);

    Bug getBugById(Long bugId);

}
