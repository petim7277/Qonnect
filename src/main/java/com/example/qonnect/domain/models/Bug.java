package com.example.qonnect.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Bug {

    private Long id;

    private String title;

    private String description;

    private BugStatus status;

    private BugPriority priority;

    private BugSeverity severity;

    private Project project;

    private User createdBy;

    private User assignedTo;

    private LocalDateTime createdAt;
}
