package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;


import com.example.qonnect.domain.models.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteUserRequest {

    private String email;

    private Role role;
}
