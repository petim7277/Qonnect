package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;

public interface ViewATaskUseCase {
    Task viewTaskInProject(User user, Long projectId, Long taskId);
}
