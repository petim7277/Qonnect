package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOrganizationAdminRequest {

    private String organizationName;

    private String firstName;

    private String lastName;


    private String email;

    private String password;
}
