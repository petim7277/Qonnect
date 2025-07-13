package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RegisterOrganizationAdminResponse {
    private String message;
    private String email;
    private String organizationName;
    private LocalDateTime createdAt;
}
