package com.example.qonnect.application.input;


import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;

public interface UpdateTaskUseCase {
    Task updateTask(User user, Long projectId, Long taskId, Task updatedTask);
}
