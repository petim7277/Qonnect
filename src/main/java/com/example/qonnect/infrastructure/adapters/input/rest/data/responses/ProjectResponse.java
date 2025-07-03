package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;
import lombok.*;

import java.time.LocalDateTime;


@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private User createdBy;
    private LocalDateTime createdAt;

}
