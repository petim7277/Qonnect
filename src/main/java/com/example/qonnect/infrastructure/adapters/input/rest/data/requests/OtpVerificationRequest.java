package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OtpVerificationRequest {
    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotBlank(message = "Otp must not be blank")
    private String otp;

}