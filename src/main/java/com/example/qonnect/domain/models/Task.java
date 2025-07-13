package com.example.qonnect.domain.models;


import com.example.qonnect.domain.models.enums.TaskStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private User assignedTo;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long projectId;
    private List<Bug> bugs;
}
