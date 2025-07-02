package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class OtpVerificationResponse {
    private String email;
    private boolean verified;
    private String message;
    private LocalDateTime verifiedAt;

    // Getters and setters
}