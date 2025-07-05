package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Task;
import com.example.qonnect.domain.models.User;

public interface CreateTaskUseCase {

    Task createTask(User user, Task task);
}
