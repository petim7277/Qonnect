package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectRequest {

    private String name;

    private String description;
}