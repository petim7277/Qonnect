package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BugResponse {
    private Long id;
    private String title;
    private String description;
    private BugStatus status;
    private BugSeverity severity;
    private Long taskId;
    private Long projectId;
    private User createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
