package com.example.qonnect.infrastructure.adapters.output.persistence.entities;

import com.example.qonnect.domain.models.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String keycloakId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(mappedBy = "teamMembers")
    private List<ProjectEntity> projects;
}
// fetch = FetchType.EAGER