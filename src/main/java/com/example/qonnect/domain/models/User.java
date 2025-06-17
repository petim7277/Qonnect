package com.example.qonnect.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private List<Project> projects;
    private Role role;

}



