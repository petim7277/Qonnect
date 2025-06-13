package com.example.qonnect.infrastructure.adapters.output.persistence.entities;

import com.example.qonnect.domain.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @ManyToMany
    @JoinTable(
            name = "project_users",
            joinColumns = @JoinColumn(name = "project_id")
    )
    private List<UserEntity> teamMembers;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<BugEntity> bugs;
}
