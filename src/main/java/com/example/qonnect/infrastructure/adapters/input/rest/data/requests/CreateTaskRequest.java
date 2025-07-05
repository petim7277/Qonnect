package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequest {

    private String title;

    private String description;

    private Long projectId;
}
