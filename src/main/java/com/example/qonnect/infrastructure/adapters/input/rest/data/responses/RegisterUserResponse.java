package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;


import com.example.qonnect.domain.models.Role;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String message;
    private Role role;
}
