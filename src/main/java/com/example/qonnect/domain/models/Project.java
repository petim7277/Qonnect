package com.example.qonnect.domain.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    private Long id;
    private String name;
    private String description;
    private User createdBy;
    private List<User> teamMembers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Task> tasks;
}
