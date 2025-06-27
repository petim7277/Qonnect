package com.example.qonnect.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Organization {

    private Long id;
    private String name;
    private List<User> users;
    private List<Project> projects;
}
