package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class UpdateTaskRequest {

    private String title;


    private String description;

    private LocalDateTime dueDate;

}
