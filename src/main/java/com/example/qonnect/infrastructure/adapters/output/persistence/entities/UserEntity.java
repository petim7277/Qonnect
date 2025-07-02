package com.example.qonnect.infrastructure.adapters.output.persistence.entities;

import com.example.qonnect.domain.models.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Getter
@Setter
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

    @Column(nullable = true)
    private boolean enabled;

    @Column(nullable = true)
    private boolean invited;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ManyToMany(mappedBy = "teamMembers")
    private List<ProjectEntity> projects;

    private LocalDateTime invitedAt;
    private String inviteToken;
    private LocalDateTime tokenExpiresAt;


    @Column(nullable = true)
    private boolean expired;
}

