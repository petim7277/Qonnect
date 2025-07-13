package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompleteInviteRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String password;
}
