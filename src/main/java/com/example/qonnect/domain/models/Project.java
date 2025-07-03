package com.example.qonnect.domain.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
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
    private Organization organization;
    private List<User> teamMembers;
    private List<Bug> bugs;

    public static void validateProjectDetails(){

    }
}
