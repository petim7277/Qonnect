package com.example.qonnect.infrastructure.adapters.output.persistence.entities;

import com.example.qonnect.domain.models.enums.BugPriority;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class BugEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private BugStatus status;

    @Enumerated(EnumType.STRING)
    private BugPriority priority;

    @Enumerated(EnumType.STRING)
    private BugSeverity severity;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskEntity task;

    private Long assignedTo;
    private LocalDateTime createdAt;
}


