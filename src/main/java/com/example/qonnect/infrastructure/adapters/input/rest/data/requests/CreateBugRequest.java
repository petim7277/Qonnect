package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import com.example.qonnect.domain.models.enums.BugPriority;
import com.example.qonnect.domain.models.enums.BugSeverity;
import com.example.qonnect.domain.models.enums.BugStatus;
import lombok.Data;

@Data
public class CreateBugRequest {

    private String title;

    private String description;

    private BugSeverity severity;

    private BugPriority priority;

    private Long projectId;
    private Long taskId;

}
