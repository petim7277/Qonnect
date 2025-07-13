package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.User;

public interface DeleteTaskUseCase {
    void deleteTask(User user, Long projectId, Long taskId);
}
