package com.example.qonnect.domain.models;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    private Long id;
    private String name;
    private List<User> users;
    private List<Project> projects;
}
