package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;


import com.example.qonnect.domain.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteUserRequest {

    private String email;

    private Role role;
}
