package com.example.qonnect.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Project {

    private Long id;

    private String name;

    private String description;
    private User createdBy;

    private List<User> teamMembers;
    private List<Bug> bugs;
}
