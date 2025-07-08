package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;

public interface AssignTaskUseCase {
    Task assignTaskToUser(User admin, Long taskId, Long assigneeId);
    Task selfAssignTask(User user, Long taskId);
}
