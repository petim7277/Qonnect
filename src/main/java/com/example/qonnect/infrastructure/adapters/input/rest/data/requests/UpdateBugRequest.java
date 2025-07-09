package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;


import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBugRequest {

    @NotNull
    private Long id;
    @Size(min = 1, max = 255, message = "Bug title must be between 1 and 255 characters")
    private String title;

    @Size(min = 1, max = 2000, message = "Bug description must be between 1 and 2000 characters")
    private String description;

    private BugStatus status;

    private BugSeverity severity;
}

