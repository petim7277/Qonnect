package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskOutputPort {

    Task saveTask(Task task);

    Task getTaskByTitle(String name);

    boolean existsByTitleAndProjectId(String title, Long projectId);


    void deleteTaskById(Long taskId);

    Task getTaskById(Long taskId);

    List<Task> getAllTasksByProjectId(Long projectId);

    Page<Task> getTasksByUserId(Long userId, Pageable pageable);
}
