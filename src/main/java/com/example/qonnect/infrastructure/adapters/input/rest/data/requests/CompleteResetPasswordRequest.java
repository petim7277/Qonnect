package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteResetPasswordRequest {



    @NotBlank(message = "OTP must not be blank")
    private String otp;

    @NotBlank(message = "New password must not be blank")
    private String newPassword;
}
