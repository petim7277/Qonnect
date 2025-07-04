package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectRequest {
    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Project description cannot be blank")
    @Size(min = 1, max = 1000, message = "Project description must be between 1 and 1000 characters")
    private String description;
}
