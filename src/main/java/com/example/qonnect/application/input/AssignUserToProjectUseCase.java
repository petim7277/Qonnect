package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.User;

public interface AssignUserToProjectUseCase {
    void assignUserToProject(Long projectId, Long userId, User performingUser);
}
