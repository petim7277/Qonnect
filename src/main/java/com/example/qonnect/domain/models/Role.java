package com.example.qonnect.domain.models;

import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;

public enum Role {

    ADMIN,
    DEVELOPER,
    QA_ENGINEER;

    public static Role from(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.EMPTY_ROLE);
        }
        try {
            return Role.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ErrorMessages.roleNotFound(input));
        }
    }

}
