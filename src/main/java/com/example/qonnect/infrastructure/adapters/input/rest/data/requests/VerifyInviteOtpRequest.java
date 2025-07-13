package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyInviteOtpRequest {

    private String inviteToken;

    private String otp;
}
