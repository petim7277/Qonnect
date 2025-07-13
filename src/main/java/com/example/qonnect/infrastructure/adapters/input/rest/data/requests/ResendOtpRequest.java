package com.example.qonnect.infrastructure.adapters.input.rest.data.requests;

import com.example.qonnect.domain.models.enums.OtpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResendOtpRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "OtpType is required")
    private OtpType otpType;

}

