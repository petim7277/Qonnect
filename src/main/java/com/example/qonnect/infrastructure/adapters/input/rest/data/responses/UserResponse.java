package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role; // Optional: store as String or Enum name
    private Long organizationId; // Optional: if you want to show which org they're in
}
