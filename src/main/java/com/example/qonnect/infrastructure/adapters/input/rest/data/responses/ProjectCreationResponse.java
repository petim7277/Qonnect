package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import com.example.qonnect.domain.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ProjectCreationResponse {
    private Long id;
    private String name;
    private String description;
    private User createdBy;
    private LocalDateTime createdAt;
}
