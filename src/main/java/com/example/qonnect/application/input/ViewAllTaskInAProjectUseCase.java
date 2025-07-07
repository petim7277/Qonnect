package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;

import java.util.List;

public interface ViewAllTaskInAProjectUseCase {

    List<Task> getAllTasksInProject(User user, Long projectId);
}
