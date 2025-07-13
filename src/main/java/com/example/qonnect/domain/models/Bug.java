package com.example.qonnect.domain.models;

import com.example.qonnect.domain.models.enums.BugPriority;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bug {
    private Long id;
    private String title;
    private String description;
    private BugSeverity severity;
    private User assignedTo;
    private BugPriority priority;
    private BugStatus status;
    private Long taskId;
    private Long projectId;
    private User createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
