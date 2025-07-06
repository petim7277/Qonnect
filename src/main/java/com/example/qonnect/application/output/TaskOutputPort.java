package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Task;

public interface TaskOutputPort {

    Task saveTask(Task task);

    Task getTaskByTitle(String name);

    boolean existsByTitleAndProjectId(String title, Long projectId);


    void deleteTaskById(Long taskId);

    Task getTaskById(Long taskId);

}
