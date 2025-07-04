package com.example.qonnect.domain.models;

import lombok.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

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
    private Long createdById;
    private Long organizationId;
    private List<User> teamMembers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Task> tasks;
    private List<Bug> bugs;

    public static void validateProjectDetails(){

    }
}
