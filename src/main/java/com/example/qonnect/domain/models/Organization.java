package com.example.qonnect.domain.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class Organization {

    private Long id;
    private String name;
    private List<User> users;
    private List<Project> projects;
}
