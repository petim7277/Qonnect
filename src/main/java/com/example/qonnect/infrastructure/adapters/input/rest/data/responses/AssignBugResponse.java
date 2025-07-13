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
public class AssignBugResponse {
    private Long id;
    private String title;
    private String description;
    private User createdBy;
    private User assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
