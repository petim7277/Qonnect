package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ViewAllUserTaskUseCase {

    Page<Task> getTasksByUserId(Long userId, Pageable pageable);
}
